package com.olam.warehouse.vegax.ghanaquality.ui.offline

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
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.work.getQualityOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ghanaquality.R
import com.olam.warehouse.vegax.ghanaquality.databinding.FragmentVegaGhanaQualityOfflineBinding
import com.olam.warehouse.vegax.ghanaquality.databinding.ItemVegaGhanaMtnrQtyOfflineBinding
import com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityViewModel
import com.olam.warehouse.vegax.ghanaquality.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaGhanaQualityOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_ghana_quality_offline
    private val vm: VegaGhanaQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaGhanaQualityOfflineBinding
    private var qualityList = arrayListOf<VegaWeighBridgeWithQualityParams>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaGhanaQualityOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            bundle: Bundle
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaQualityOfflineBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("offloadingnigeria_sesame/ui/offline/VegaNigeriaSesameOffloadingOfflineSummaery")
            .title("Vega_Ecuador/offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) activity?.finish()
            else showDialog()
        }
        vm.wbWithParams.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getGhanaCashewWBWithQuality("PROCURE")
    }

    private fun updateUI(data: List<VegaWeighBridgeWithQualityParams>) {
        data.let { quality ->
            val qtyList = arrayListOf<VegaWeighBridgeWithQualityParams>()
            when {
                quality.isNotEmpty() -> {

                    qtyList.addAll(quality)
                    setUpAdapter(qtyList)
                }
                else -> {
                    setUpAdapter(qtyList)
                    setErrorContentView("No data available")
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_quality_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    startSync(qualityList)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(qtyList: ArrayList<VegaWeighBridgeWithQualityParams>) {
        qualityList = qtyList
        changeBtn(qualityList)
        if (qualityList.size > 0) {
            binding.tvNoData.gone()
            binding.rvOfflineQuality.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvOfflineQuality.gone()
        }
        binding.rvOfflineQuality.setUpAdapter(
            qualityList,
            R.layout.item_vega_ghana_qty_offline,
            ItemVegaGhanaMtnrQtyOfflineBinding::inflate,
            { it, pos, bindItem ->
                val quality = it.qualityWBDetails
                val status = quality.status ?: 1
                bindItem.tvGradeItem.text = quality.materialName
                bindItem.tvWeighBridgeNo.text = quality.weighBridgeId

                val isError = quality.isErrorStatus
                if (isError) {
                    bindItem.llError.gone()
                } else {
                    bindItem.tvErrorMsg.text = quality.message
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
                    if (quality.finalApproval?.equals(FNQUALITY)!!) {
                        //ivDeleteData.gone()
                        bindItem.tvViewWeighBridge.isEnabled = false
                        bindItem.tvViewWeighBridge.text =
                            getString(com.olam.warehouse.presentation.R.string.lot_no).plus("\n")
                                .plus(quality.message)
                    } else {
                        bindItem.tvViewWeighBridge.isEnabled = false
                        bindItem.tvViewWeighBridge.text = "Rejected"
                    }
                } else {
                    //ivDeleteData.visible()
                    bindItem.tvViewWeighBridge.isEnabled = true
                    bindItem.tvViewWeighBridge.text = context.getString(R.string.view_details)
                }
                bindItem.tvViewWeighBridge.setOnClickListener { view -> ViewDetails(it) }
                bindItem.ivDeleteData.setOnClickListener { view -> showDeleteDialog(it) }

            })
    }

    private fun ViewDetails(it: VegaWeighBridgeWithQualityParams) {
        val weighBridge = it.qualityWBDetails
        val batchNo = if (weighBridge.batchNumber.isNullOrEmpty()) "" else weighBridge.batchNumber.toString()
        val bundle = Bundle().apply {
            putString(WEIGHBRIDGE_LIST, weighBridge.weighBridgeId)
            putBoolean(IS_PARAMS_VALUE, true)
            putString(BATCH_NO, batchNo)
            putString(NET_WEIGHT, weighBridge.netWeight)
            putString(GROSS_WEIGHT, weighBridge.grossWeight)
            putString(TAR_WEIGHT, weighBridge.bagWeight)
            putString(MATERIAL_NO, weighBridge.materialCode)
            putParcelable(WEIGHSCALE, weighBridge)
        }
        callBack?.replaceFragment(
            PARAMS_LIST, bundle
        )
    }

    private fun showDeleteDialog(item: VegaWeighBridgeWithQualityParams) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    qualityList.remove(item)
                    vm.updateDeletedItem(item.qualityWBDetails.weighBridgeId)
                },
                { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<VegaWeighBridgeWithQualityParams>) {
        val gson = Gson()
        item.forEachIndexed { _index, _element ->
            if (_element.qualityWBDetails.status != 4) {
                val input = workDataOf(UIUtils.QUALITY_DATA to _element.qualityWBDetails.weighBridgeId)
                val worker = getQualityOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(viewLifecycleOwner, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
//                                    val position = workInfo.tags.first().toInt()
                                    //mAdapter.setSyncStatus(position, true, workInfo.outputData)
                                    if (_index == this.qualityList.size - 1) {
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
                                    if (_index == this.qualityList.size - 1) {
                                        changeBtn(item)
                                    }
                                }
                                WorkInfo.State.RUNNING -> showLoading()
                                else -> {}
                            }
                        }

                    })
                hideLoading()
            }
        }
    }

    fun changeBtn(item: ArrayList<VegaWeighBridgeWithQualityParams>) {
        val count = qualityList.filter { it.qualityWBDetails.status == 4 }
        if (qualityList.size == count.size)
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
        else if (qualityList.any { it.qualityWBDetails.status == 3 })
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.re_sync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }
}
