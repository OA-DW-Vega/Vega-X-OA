package com.olam.warehouse.vegax.dispatchecuador.ui.offline

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchLotsMerge
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.master.work.getEcuadorDispatchOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_POST_DATA
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchecuador.R
import com.olam.warehouse.vegax.dispatchecuador.databinding.FragmentEcuadorDispatchOfflineSummaryBinding
import com.olam.warehouse.vegax.dispatchecuador.databinding.ItemVegaEcuadorDispatchOfflineSummaryBinding
import com.olam.warehouse.vegax.dispatchecuador.ui.VegaEcuadorDispatchViewModel
import com.olam.warehouse.vegax.dispatchecuador.utils.ADD_LOT
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaEcuadorDispatchOfflineSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ecuador_dispatch_offline_summary
    private val vm: VegaEcuadorDispatchViewModel by viewModel()

    private lateinit var binding: FragmentEcuadorDispatchOfflineSummaryBinding
    private var dispatchItems = arrayListOf<VegaEcuadorDispatchWithLineItems>()
    private var callBack: CallBack? = null
    private var mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorDispatchLots>>()
    private var deliveryDetails = ArrayList<VegaEcuadorDispatchLotsMerge>()
    private var dispatchLotsList = mutableListOf<VegaEcuadorDispatchLots>()

    companion object {
        fun newInstance() = VegaEcuadorDispatchOfflineSummaryFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            dispatchData: VegaEcuadorDispatch,
            data: Any
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEcuadorDispatchOfflineSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("dispatchecuador/ui/offline/VegaEcuadorDispatchOfflineSummary")
            .title("Vega_Ecuador/dispatch")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        vm.dispatchItemLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getDispatchWithLineItem()

        if (AppUtils.isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    binding.btnSyncProceed.context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(
                ContextCompat.getColor(
                    binding.btnSyncProceed.context,
                    com.olam.warehouse.presentation.R.color.grey
                )
            )

        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) activity?.onBackPressed()
            else showDialog()
        }
    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    startSync(dispatchItems)
                },
                { dismiss() })
        }
    }

    private fun updateUI(data: List<VegaEcuadorDispatchWithLineItems>?) {
        data?.let { dispatch ->
            val dispatches = arrayListOf<VegaEcuadorDispatchWithLineItems>()
            when {
                dispatch.isNotEmpty() -> {
                    dispatches.addAll(dispatch)
                    setUpAdapter(dispatches)
                }
                else -> {
                    setUpAdapter(dispatches)
                    setErrorContentView(getString(R.string.no_data_found))
                }
            }
        }
    }

    private fun setUpAdapter(dispatchItemsList: ArrayList<VegaEcuadorDispatchWithLineItems>) {
        dispatchItems = dispatchItemsList
        changeBtn()
        if (dispatchItems.size > 0) {
            binding.tvNoData.gone()
            binding.rvDispatchOffline.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvDispatchOffline.gone()
        }
        binding.rvDispatchOffline.setUpAdapter(
            dispatchItems,
            R.layout.item_vega_ecuador_dispatch_offline_summary,
            ItemVegaEcuadorDispatchOfflineSummaryBinding::inflate,
            { it, pos, bindItem ->
                val dispatch = it.dispatch
                bindItem.tvStoNo.text = dispatch.purchaseDocNum
                bindItem.tvDestinationWH.text =
                    dispatch.storageLocationCode.plus("-").plus(dispatch.plantName)
                bindItem.tvDeliveryId.text = dispatch.deliveryId
                when (dispatch.status) {
                    Status.MTNT_COMPLETED -> {
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.do_icon_completed))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindItem.tvError.gone()
                        bindItem.tvViewDetails.isEnabled = false
                    }
                    Status.SYNC_ERROR -> {
                        bindItem.tvViewDetails.isEnabled = true
                        bindItem.tvError.visible()
                        bindItem.tvError.text = dispatch.syncStatusMsg
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_do_icon_error))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(3)
                            )
                        )
                    }
                    else -> {
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.icon_no_progresss))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        bindItem.tvError.gone()
                        bindItem.tvViewDetails.isEnabled = true
                    }
                }
                bindItem.ivDelete.setOnClickListener { view ->
                    showDeleteDialog(it)
                }
                bindItem.tvViewDetails.setOnClickListener { view ->
                    if (dispatch.status != Status.MTNT_COMPLETED) viewDetails(it)
                }
            })
    }

    private fun viewDetails(it: VegaEcuadorDispatchWithLineItems) {
        callBack?.replaceFragment(
            ADD_LOT,
            it.dispatch,
            it.lineItems as ArrayList<VegaEcuadorDispatchLots>
        )
    }

    private fun showDeleteDialog(item: VegaEcuadorDispatchWithLineItems) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    dispatchItems.remove(item)
                    vm.updateDeletedItem(item.dispatch.wbTempId)
                },
                { dismiss() })
        }
    }

    private fun changeBtn() {
        val count = dispatchItems.filter { it.dispatch.status.equals(Status.MTNT_COMPLETED) }
        if (dispatchItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (dispatchItems.any { it.dispatch.status.equals(Status.SYNC_ERROR) })
            binding.btnSyncProceed.text = getString(R.string.resync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }

    private fun startSync(item: ArrayList<VegaEcuadorDispatchWithLineItems>) {
        item.forEachIndexed { _index, _element ->
            if (!_element.dispatch.status.equals(Status.MTNT_COMPLETED)) {
                val input = workDataOf(
                    DISPATCH_DATA to _element.dispatch.wbTempId,
                    DISPATCH_POST_DATA to _element.deliveryDetails
                )
                val worker = getEcuadorDispatchOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    if (_index == this.dispatchItems.size - 1) {
                                        changeBtn()
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    this.dispatchItems[_index].deliveryDetails =
                                        workInfo.outputData.getString(DISPATCH_OUTPUT_DATA)
                                    if (_index == this.dispatchItems.size - 1) {
                                        changeBtn()
                                    }
                                }
                                WorkInfo.State.RUNNING -> showLoading()
                                else -> {
                                }
                            }
                        }

                    })
                hideLoading()
            }
        }
    }

}
