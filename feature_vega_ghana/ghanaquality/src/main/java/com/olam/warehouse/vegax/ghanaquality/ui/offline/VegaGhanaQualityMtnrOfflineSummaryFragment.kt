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
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.work.getMtnrQualityOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ghanaquality.R
import com.olam.warehouse.vegax.ghanaquality.databinding.FragmentVegaGhanaQualityOfflineBinding
import com.olam.warehouse.vegax.ghanaquality.ui.VegaGhanaQualityViewModel
import com.olam.warehouse.vegax.ghanaquality.utils.*
import kotlinx.android.synthetic.main.item_vega_ghana_mtnr_qty_offline.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaQualityMtnrOfflineSummaryFragment : BaseFragment() {
    private var selectedWBDetails = VegaQualityWBDetails()
    private var lotDetails = VegaGhanaMtnrQualityLot()
    private var completeLotDetails = arrayListOf<VegaGhanaMtnrQualityLot>()
    override val layoutResourceId = R.layout.fragment_vega_ghana_mtnr_quality_offline
    private val vm: VegaGhanaQualityViewModel by viewModel()
    private lateinit var binding: FragmentVegaGhanaQualityOfflineBinding
    private var qualityList = arrayListOf<VegaWeighBridgeWithQualityParams>()
    private var callBack: CallBack? = null

    companion object {
        fun newInstance() = VegaGhanaQualityMtnrOfflineSummaryFragment().putArgs {
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
        if (AppUtils.isOnline()) {
            binding.btnSyncProceed.isEnabled = true
            binding.btnSyncProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnSyncProceed.isEnabled = false
            binding.btnSyncProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
        binding.btnSyncProceed.setOnClickListener {
            val btnText = binding.btnSyncProceed.text
            if (btnText.contains("OK")) {
                showDeleteMessage()
                vm.deleteWBDetals()
//                activity?.finish()
            } else showDialog()
        }
        vm.wbWithParams.observe(this, Observer { updateUI(it) })
        //        vm.getWBWithQuality("")
        vm.getGhanaCashewWBWithQuality("STO")

        vm.mtnrLotList.observe(this, Observer {
            lotDetails = it
            viewDetails()
        })

    }

    private fun showDeleteMessage() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_mtnt)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.okconfirm),
                    isPositive = true
                )
            ) {
//                activity?.onBackPressed()
                activity?.finish()
            }
        }
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
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(qualityList, 0)
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
            R.layout.item_vega_ghana_mtnr_qty_offline,
            { it, pos ->
                val quality = it.qualityWBDetails
                val status = quality.status ?: 1
                tvGradeItem.text = quality.materialName
                tvReceivedWeight.text = quality.paidWeight.plus(" ").plus("MT")
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
                    if (quality.finalApproval?.equals(FNQUALITY)!!) {
                        //ivDeleteData.gone()
                        tvViewWeighBridge.isEnabled = false
                        tvViewWeighBridge.text =
                            getString(com.olam.warehouse.presentation.R.string.grn_no).plus("\n").plus(quality.message)
                    } else {
                        tvViewWeighBridge.isEnabled = false
                        tvViewWeighBridge.text = "Rejected"
                    }
                } else {
                    //ivDeleteData.visible()
                    tvViewWeighBridge.isEnabled = true
                    tvViewWeighBridge.text = context.getString(R.string.view_details)
                }
                tvViewWeighBridge.setOnClickListener { view ->
                    vm.getLotQualityDetails(it.qualityWBDetails.wbTempId)
                    selectedWBDetails = it.qualityWBDetails
                }
                ivDeleteData.setOnClickListener { view -> showDeleteDialog(it) }

            })
    }

    // private fun viewDetails(it: VegaWeighBridgeWithQualityParams) {
    private fun viewDetails() {
        val wbDetails = selectedWBDetails
//        val wbDetails = it.qualityWBDetails
        var lotItems = arrayListOf<VegaCoffeeLot>()
//        lotItems.add(lotDetails as VegaCoffeeLot)
        lotItems.add(prepareViewData(lotDetails))
        val bundle = Bundle().apply {
            putBoolean(IS_PARAMS_VALUE, false)
            putParcelable(LOT, prepareViewData(lotDetails))
            putParcelableArrayList(LOT_LIST, lotItems)
            putParcelable(WEIGHSCALE, wbDetails)
        }

        callBack?.replaceFragment(
            PARAMS_LIST_MTNR_VIEW_DETAILS, bundle
        )
    }

    private fun showDeleteDialog(item: VegaWeighBridgeWithQualityParams) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    qualityList.remove(item)
                    vm.updateDeletedItem(item.qualityWBDetails.weighBridgeId)
                },
                { dismiss() })
        }
    }

    var syncCount = 0

    private fun startSync(item: ArrayList<VegaWeighBridgeWithQualityParams>, _index: Int) {
        val gson = Gson()
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]
//        item.forEachIndexed { _index, _element ->
        if (_element.qualityWBDetails.status != 4) {
            val input = workDataOf(UIUtils.QUALITY_DATA to _element.qualityWBDetails.weighBridgeId)
            val worker = getMtnrQualityOneTimeRequestWorker(input, _index)
            enQueueWorker(worker, requireContext())
            WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                .observe(this, Observer { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                val position = AppUtils.posExtension(workInfo.tags) + 1
                                syncCount++
                                if (syncCount != item.size) startSync(item, position)
                                if (syncCount == item.size) {
                                    syncCount = 0
                                    PreferenceHelper.save(Constants.START_SYNC, false)
                                }

//                                    hideLoading()
//                                    val position = workInfo.tags.first().toInt()
                                //mAdapter.setSyncStatus(position, true, workInfo.outputData)
                                if (_index == this.qualityList.size - 1) {
                                    changeBtn(item)
                                }
                            }
                            WorkInfo.State.FAILED -> {
//                                    hideLoading()
//                                    val position = workInfo.tags.first().toInt()
                                /*mAdapter.setSyncStatus(position, false, workInfo.outputData)
                                vm.updateReceivingFailMsg(
                                    workInfo.outputData.getString(RECEIVING_OUTPUT_DATA).toString(),
                                    this.receivings[position].receiving.tmpWbId
                                )*/
                                val position = AppUtils.posExtension(workInfo.tags) + 1
                                syncCount++
                                if (syncCount != item.size) startSync(item, position)
                                if (syncCount == item.size) {
                                    syncCount = 0
                                    PreferenceHelper.save(Constants.START_SYNC, false)
                                }

                                if (_index == this.qualityList.size - 1) {
                                    changeBtn(item)
                                }
                            }
                            WorkInfo.State.RUNNING -> showLoading()
                        }
                    }

                })
//                hideLoading()
//            }
        }
    }

    fun changeBtn(item: ArrayList<VegaWeighBridgeWithQualityParams>) {
        hideLoading()
        val count = qualityList.filter { it.qualityWBDetails.status == 4 }
        if (qualityList.size == count.size)
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
        else if (qualityList.any { it.qualityWBDetails.status == 3 })
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.re_sync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }
}
