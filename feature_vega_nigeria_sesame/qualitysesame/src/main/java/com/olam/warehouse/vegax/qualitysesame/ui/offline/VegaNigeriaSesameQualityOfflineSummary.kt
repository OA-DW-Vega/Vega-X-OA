package com.olam.warehouse.vegax.qualitysesame.ui.offline

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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.work.getQualityOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitysesame.R
import com.olam.warehouse.vegax.qualitysesame.databinding.FragmentVegaNigeriaSesameQualityOfflineBinding
import com.olam.warehouse.vegax.qualitysesame.ui.VegaNigeriaSesameQualityViewModel
import com.olam.warehouse.vegax.qualitysesame.utils.*
import kotlinx.android.synthetic.main.item_vega_nigeria_sesame_qty_offline.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaNigeriaSesameQualityOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nigeria_sesame_quality_offline
    private val vm: VegaNigeriaSesameQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaSesameQualityOfflineBinding
    private var qualityList = arrayListOf<VegaWeighBridgeWithQualityParams>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaNigeriaSesameQualityOfflineSummary().putArgs {
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
        binding = FragmentVegaNigeriaSesameQualityOfflineBinding.inflate(layoutInflater)
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
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnSyncProceed, it, true)
        }
        if (isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(
                getColor(
                    if (getCurrentOriginEntity().contains(
                            "OFI"
                        )
                    ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
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
        vm.wbWithParams.observe(this, Observer { updateUI(it) })
        vm.getWBWithQualityPars("")
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
                getString(R.string.proceed),
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
        binding.rvOfflineQuality.setUp(
            qualityList,
            R.layout.item_vega_nigeria_sesame_qty_offline,
            { it, pos ->
                val quality = it.qualityWBDetails
                val status = quality.status ?: 1
                tvGradeItem.text = quality.materialName
                tvWeighBridgeNo.text = quality.weighBridgeId

                val isError = quality.isErrorStatus
                if (isError) {
                    llError.gone()
                } else {
                    tvErrorMsg.text = quality.message
                    llError.visible()
                }

                ivStatus.setImageResource(UIUtils.getSyncStatusIcon(status))
                vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        UIUtils.getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    //ivDeleteData.gone()
                    tvViewWeighBridge.isEnabled = false
                    tvViewWeighBridge.text =
                        getString(com.olam.warehouse.presentation.R.string.lot_no).plus("\n").plus(quality.message)
                } else {
                    //ivDeleteData.visible()
                    tvViewWeighBridge.isEnabled = true
                    tvViewWeighBridge.text = context.getString(R.string.view_details)
                }
                tvViewWeighBridge.setOnClickListener { view -> ViewDetails(it) }
                ivDeleteData.setOnClickListener { view -> showDeleteDialog(it) }

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
                getString(R.string.proceed),
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
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    hideLoading()
                                    val position = workInfo.tags.first().toInt()
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
                                else -> {
                                }
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
