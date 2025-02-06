package com.olam.warehouse.vegax.offloadingnigeria.ui.offline

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
import com.google.gson.Gson
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.master.work.getOffloadingOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ActivityVegaNigeriaOffloadingOfflineSummaryBinding
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ItemVagaNigeriaOffloadingOfflineSummaryBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.OFFLOADING_SUMMARY_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaNigeriaOffloadingOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.activity_vega_nigeria_offloading_offline_summary
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private lateinit var binding: ActivityVegaNigeriaOffloadingOfflineSummaryBinding
    private var offloadingItems = arrayListOf<VegaEcuaOffloadingWithLineItems>()
    private var callBack: CallBack? = null
    private var mReceiving = mutableListOf<VegaReceiving>()

    companion object {
        fun newInstance() = VegaNigeriaOffloadingOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityVegaNigeriaOffloadingOfflineSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingecuador/ui/offline/VegaEcuadorOffloadingOfflineSummaery")
            .title("Vega_Ecuador/offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        vm.offloadingItemLocal.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getOffloadingWithLineItem()
        /* binding.rvReceivingOffline.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
         binding.rvReceivingOffline.adapter = mAdapter*/

        if (isOnline()) {
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

    private fun updateUI(data: List<VegaEcuaOffloadingWithLineItems>?) {
        data?.let { receiving ->
            val offloadings = arrayListOf<VegaEcuaOffloadingWithLineItems>()
            when {
                receiving.isNotEmpty() -> {

                    offloadings.addAll(receiving)
                    setUpAdapter(offloadings)
                }
                else -> {
                    setUpAdapter(offloadings)
                    setErrorContentView("No data available")
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_offloading)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(offloadingItems)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(offloadingItems1: ArrayList<VegaEcuaOffloadingWithLineItems>) {
        offloadingItems = offloadingItems1
        changeBtn(offloadingItems)
        if (offloadingItems.size > 0) {
            binding.tvNoData.gone()
            binding.rvReceivingOffline.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvReceivingOffline.gone()
        }
        binding.rvReceivingOffline.setUpAdapter(
            offloadingItems,
            R.layout.item_vaga_nigeria_offloading_offline_summary,
            ItemVagaNigeriaOffloadingOfflineSummaryBinding::inflate,
            { it, pos, bindItem ->
                val receiving = it.receiving
                bindItem.tvNoOfBags.text = receiving.bagCount
                bindItem.tvWeight.text =
                    receiving.netWeight.plus(" ").plus(receiving.unitsOfMeasure)
                bindItem.tvProductType.text = receiving.materialName
                bindItem.tvWbId.text = receiving.weighBridgeId
                //viewStatus.isVisible = receiving.isSynced
                when (receiving.status) {
                    Status.RECEVING_COMPLETED -> {
                        bindItem.ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.do_icon_completed))
                        bindItem.viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        bindItem.tvError.gone()
                        bindItem.tvViewDetails.text =
                            getString(com.olam.warehouse.login.R.string.print_preview)
                        bindItem.tvViewDetails.isEnabled = true
                    }
                    Status.SYNC_ERROR -> {
                        bindItem.tvViewDetails.isEnabled = true
                        bindItem.tvError.visible()
                        bindItem.tvError.text = receiving.syncStatusMsg
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
                    if (receiving.status != Status.RECEVING_COMPLETED) ViewDetails(it)
                    else {
                        val tallyKeys = ArrayList<String>()
                        tallyKeys.add(it.receiving.encodedImageContent ?: "")
                        showPreviewDialog(tallyKeys)
                    }
                }
            })
    }

    private fun ViewDetails(data: VegaEcuaOffloadingWithLineItems) {
        mReceiving.clear()
        data.lineItems.forEachIndexed { index, it ->
            val receiving = data.receiving.copy()
            receiving.bagType = it.bagType
            receiving.bagCount = it.bagCount
            receiving.bagWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())!!.formatThreeDigits()
            receiving.netWeight = it.netWeight
            receiving.grossWeight = it.grossWeight
            receiving.item = index.inc().toString()
            mReceiving.add(receiving)
        }
        callBack?.replaceFragment(
            OFFLOADING_SUMMARY_FRAG,
            data.receiving,
            mReceiving as ArrayList<VegaReceiving>,
            data.lineItems as ArrayList<VegaEcuadorOffloadingBagMaterial>
        )
    }

    private fun showDeleteDialog(item: VegaEcuaOffloadingWithLineItems) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    offloadingItems.remove(item)
                    vm.updateDeletedItem(item.receiving.tmpWbId)
                },
                { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<VegaEcuaOffloadingWithLineItems>) {
        val gson = Gson()
        item.forEachIndexed { _index, _element ->
            if (!_element.receiving.status.equals(Status.RECEVING_COMPLETED)) {
                val input = workDataOf(RECEIVING_DATA to _element.receiving.tmpWbId)
                val worker = getOffloadingOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
                                    //mAdapter.setSyncStatus(position, true, workInfo.outputData)
                                    if (_index == this.offloadingItems.size - 1) {
                                        changeBtn(item)
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
                                    /*mAdapter.setSyncStatus(position, false, workInfo.outputData)
                                    vm.updateReceivingFailMsg(
                                        workInfo.outputData.getString(RECEIVING_OUTPUT_DATA).toString(),
                                        this.receivings[position].receiving.tmpWbId
                                    )*/
                                    if (_index == this.offloadingItems.size - 1) {
                                        changeBtn(item)
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

    fun changeBtn(item: ArrayList<VegaEcuaOffloadingWithLineItems>) {
        val count = offloadingItems.filter { it.receiving.status.equals(Status.RECEVING_COMPLETED) }
        if (offloadingItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (offloadingItems.any { it.receiving.status.equals(Status.SYNC_ERROR) })
            binding.btnSyncProceed.text = getString(R.string.resync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }

    private fun showPreviewDialog(tallyPrintKeys: ArrayList<String>) {
        val list = mutableListOf<String>()
        list.addAll(tallyPrintKeys)
        val dialogFragment = PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { it1 -> dialogFragment.show(it1, "signature") }
    }

}
