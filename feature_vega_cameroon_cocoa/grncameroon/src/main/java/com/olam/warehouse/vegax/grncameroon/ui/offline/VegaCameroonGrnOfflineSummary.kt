package com.olam.warehouse.vegax.grncameroon.ui.offline

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
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.work.getGrnOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grncameroon.R
import com.olam.warehouse.vegax.grncameroon.databinding.FragmentVegaCameroonGrnOfflineBinding
import com.olam.warehouse.vegax.grncameroon.databinding.ItemVegaCameroonGrnOfflineBinding
import com.olam.warehouse.vegax.grncameroon.ui.VegaCameroonGrnViewModel
import com.olam.warehouse.vegax.grncameroon.utils.GRN_FRAG
import com.olam.warehouse.vegax.grncameroon.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaCameroonGrnOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_cameroon_grn_offline
    private val vm: VegaCameroonGrnViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonGrnOfflineBinding
    private var grnList = arrayListOf<VegaGrnWeighBridgeId>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaCameroonGrnOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonGrnOfflineBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grncameroon/ui/offline/VegaCameroonGrnOfflineSummary")
            .title("Vega_Cameroon/Grn")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnSyncProceed, it, true)
        }
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) activity?.finish()
            else showDialog()
        }
        vm.weighBridgeOffline.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getOfflineWeighBridgeDetail()
    }

    private fun updateUI(data: List<VegaGrnWeighBridgeId>) {
        data.let { quality ->
            val grnList = arrayListOf<VegaGrnWeighBridgeId>()
            when {
                quality.isNotEmpty() -> {
                    grnList.addAll(quality)
                    setUpAdapter(grnList)
                }
                else -> {
                    setUpAdapter(grnList)
                    setErrorContentView(getString(R.string.msg_no_data_available))
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_grn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(grnList)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(qtyList: ArrayList<VegaGrnWeighBridgeId>) {
        grnList = qtyList
        changeBtn()
        if (grnList.size > 0) {
            binding.tvNoData.gone()
            binding.rvOfflineGrn.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvOfflineGrn.gone()
        }
        binding.rvOfflineGrn.setUpAdapter(
            grnList,
            R.layout.item_vega_cameroon_grn_offline,
            ItemVegaCameroonGrnOfflineBinding::inflate,
            { it, pos, bindItem ->
                val status = it.status ?: 1
                bindItem.tvGradeItem.text = it.materialName
                bindItem.tvWeighBridgeNo.text = it.weighBridgeId

                val isError = it.isErrorStatus
                if (isError) {
                    bindItem.llError.gone()
                } else {
                    bindItem.tvErrorMsg.text = it.message
                    bindItem.llError.visible()
                }

                bindItem.ivStatus.setImageResource(UIUtils.getSyncStatusIcon(status))
                bindItem.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        UIUtils.getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    bindItem.tvViewWeighBridge.isEnabled = false
                    bindItem.tvViewWeighBridge.text = "Grn No \n".plus(it.grnNumber)
                } else {
                    bindItem.tvViewWeighBridge.isEnabled = true
                    bindItem.tvViewWeighBridge.text = context.getString(R.string.view_details)
                }
                bindItem.tvViewWeighBridge.setOnClickListener { view -> ViewDetails(it) }
                bindItem.ivDeleteData.setOnClickListener { view -> showDeleteDialog(it) }


            })
    }

    private fun ViewDetails(it: VegaGrnWeighBridgeId) {
        val weighBridge = it
        callBack?.replaceFragment(
            GRN_FRAG, weighBridge
        )
    }

    private fun showDeleteDialog(item: VegaGrnWeighBridgeId) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    grnList.remove(item)
                    vm.updateDeletedItem(item.weighBridgeId.toString())
                },
                { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<VegaGrnWeighBridgeId>) {
        item.forEachIndexed { _index, _element ->
            if (_element.status != 4) {
                val input = workDataOf(UIUtils.GRN_DATA to _element.weighBridgeId)
                val worker = getGrnOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    if (_index == this.grnList.size - 1) {
                                        changeBtn()
                                    }
                                }
                                WorkInfo.State.FAILED -> {
                                    hideLoading()
                                    if (_index == this.grnList.size - 1) {
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

    fun changeBtn() {
        val count = grnList.filter { it.status == 4 }
        if (grnList.size == count.size)
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
        else if (grnList.any { it.status == 3 })
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.re_sync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }
}
