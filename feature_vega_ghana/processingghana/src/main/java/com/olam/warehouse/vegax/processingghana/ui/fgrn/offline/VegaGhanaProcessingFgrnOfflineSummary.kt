package com.olam.warehouse.vegax.processingghana.ui.fgrn.offline

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
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineFgrn
import com.olam.warehouse.master.work.getFgrnOneTimeRequestWorker
import com.olam.warehouse.master.work.getRminOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.FGRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.FGRN_STAGE
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_OUTPUTMATERIALCODE
import com.olam.warehouse.presentation.utils.UIUtils.RMIN_VERSION
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.ActivityVegaGhanaProcessingFgrnOfflineSummaryBinding
import com.olam.warehouse.vegax.processingghana.ui.fgrn.VegaGhanaFgrnViewModel
import com.olam.warehouse.vegax.processingghana.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.processingghana.utils.OFFLINE_FGRN_SUMMARY_FRAG
import kotlinx.android.synthetic.main.item_vaga_ghana_processing_fgrn_offline_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaGhanaProcessingFgrnOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.activity_vega_ghana_processing_fgrn_offline_summary
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private lateinit var binding: ActivityVegaGhanaProcessingFgrnOfflineSummaryBinding
    private var offloadingItems = arrayListOf<VegaGhanaOfflineFgrnProcessLotDetails>()
    private var callBack: CallBack? = null
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var fgrnData = arrayListOf<VegaGhanaOfflineFgrnData>()
    private var stockList = VegaEcuadorDispatchStocks()
    private var vegaStage = VegaProcessingStage()
    private var versionID: String? = ""
    private var OutputMaterialCode: String = ""

    companion object {
        fun newInstance(vegastage: VegaProcessingStage) =
            VegaGhanaProcessingFgrnOfflineSummary().putArgs {
                putParcelable(MODEL_BUNDLE, vegastage)
            }
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            rminLots: List<VegaGhanaOfflineFgrnProcessLotDetails>,
            lotData: List<VegaGhanaOfflineFgrnData>, data: String, vegaStage: VegaProcessingStage
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityVegaGhanaProcessingFgrnOfflineSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingghana/ui/rmin/offline/VegaGhanaProcessingRminOfflineSummary")
            .title("Vega_Ecuador/offloading")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        vegaStage = arguments?.getParcelable(MODEL_BUNDLE)!!
        vm.offlineFgrnItemLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getOfflineFgrnItem()

        vm.stockLot.observe(this, Observer {
            if (it != null) {
                stockList = it
            }
        })

        vm.fgrnItemLocal.observe(this, Observer {
            fgrnData = it as ArrayList<VegaGhanaOfflineFgrnData>
        })
        vm.getofflineFgrnPostItem()

        vm.vegaOfflineRMINItems.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                versionID = it.get(0).versionId
                OutputMaterialCode = it.get(0).outputMaterialCode!!
            }
        })


//        vm.offlineRminLotsLocal.observe(this, Observer {
//            rminLots = it as MutableList<VegaGhanaOfflineRminLots>
//        })
//        vm.getOfflineRminLots()
//
//        vm.offlineRminItemsLocal.observe(this, Observer {
//            rminItems = it as MutableList<VegaGhanaOfflineRminItems>
//        })
//        vm.getOfflineRminItems()
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
            if (btnText.contains("OK")) {
                vm.updateSyncedFgrnDeletedItem(4)
                showDeleteMessage()
//                activity?.onBackPressed()
            } else showDialog()
        }
    }

    private fun showDeleteMessage() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_fgrn)
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

    private fun updateUI(data: List<VegaGhanaOfflineFgrn>?) {
        data?.let { receiving ->
            val offline = arrayListOf<VegaGhanaOfflineFgrnProcessLotDetails>()
            when {
                receiving.isNotEmpty() -> {
                    receiving.forEach {

                        offline.addAll(it.fgrnLot)
                    }

                    setUpAdapter(offline)
                }
                else -> {
                    setUpAdapter(offline)
                    setErrorContentView("No data available")
                }
            }
        }

    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_fgrn)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /* offloadingItems.forEach { if(it.processOrderNum!!.contains("TMP_RMIN_"))
                                        startRMINSync(offloadingItems,0)
                                    else*/
                    startSync(offloadingItems, 0)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(offloadingItems1: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>) {
        offloadingItems = offloadingItems1
        changeBtn(offloadingItems)
        if (offloadingItems.size > 0) {
            binding.tvNoData.gone()
            binding.rvReceivingOffline.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvReceivingOffline.gone()
        }
        binding.rvReceivingOffline.setUp(
            offloadingItems,
            R.layout.item_vaga_ghana_processing_fgrn_offline_summary,
            { it, pos ->
                val receiving = it
                val status = receiving.status ?: 1
                tvNoOfBags.text = receiving.bagCount
                tvWeight.text = receiving.netWeight.plus(" ").plus("MT")
                tvProductType.text = receiving.rminTempId
                tvWbId.text = receiving.fgrnTempId
                //viewStatus.isVisible = receiving.isSynced
                if (receiving.processOrderNum!!.contains("TMP_RMIN_"))
                    vm.getofflineRMINVersionID(receiving.rminTempId!!)
                ivStatus.setImageResource(UIUtils.getSyncStatusIcon(status))
                if (receiving.successMessage?.isNotEmpty() == true) {
                    llMsg.visible()
                    tvSuccessMsg.text = receiving.successMessage
                } else {
                    llMsg.gone()
                }
                viewStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        UIUtils.getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    tvError.gone()
                    tvViewDetails.text = "Success"
                    tvViewDetails.isEnabled = false
                } else if (status == 3) {
                    tvViewDetails.isEnabled = true
                    tvError.visible()
                    tvError.text = receiving.syncStatusMsg
                } else {
                    tvError.gone()
                    tvViewDetails.isEnabled = true
                }

                ivDelete.setOnClickListener { view ->
                    vm.getStockDetailsByBatchNo(it.batchNumber!!)
                    showDeleteDialog(it)
                }
                tvViewDetails.setOnClickListener { view ->
                    if (receiving.status != 4) ViewDetails(it.fgrnTempId)
//                    else {
//                        val tallyKeys = ArrayList<String>()
//                        tallyKeys.add(it.receiving.encodedImageContent ?: "")
//                        showPreviewDialog(tallyKeys)
//                    }
                }
            })
    }

    private fun ViewDetails(data: String) {
        var lotList = offloadingItems.filter { it.fgrnTempId == data }
        var lotData = fgrnData.filter { it.fgrnTempId == data }
        var data = ""
        callBack?.replaceFragment(
            OFFLINE_FGRN_SUMMARY_FRAG,
            lotList,
            lotData,
            data, vegaStage
        )
    }

    private fun showDeleteDialog(item: VegaGhanaOfflineFgrnProcessLotDetails) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (stockList.batchNumber != "") {
                        var updatedWeight =
                            stockList.weight?.toDouble()!! - item.netWeight?.toDouble()!!
                        vm.updateStockDetails(updatedWeight.toString(), item.batchNumber!!)
                    }
                    offloadingItems.remove(item)
                    vm.updateDeletedItem(item.fgrnTempId)
                    vm.updateFgrnStatus(false, item.rminTempId!!)
                },
                { dismiss() })
        }
    }

    var syncCount = 0
    private fun startSync(item: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]
        //grnList1.forEachIndexed { _index, _element ->
        // val input = workDataOf(FGRN_DATA to _element.fgrnTempId)
        // val input_stage = workDataOf(FGRN_STAGE to vegaStage)
        val input = workDataOf(
            FGRN_DATA to _element.fgrnTempId,
            FGRN_STAGE to vegaStage.cfgNo,
            RMIN_VERSION to versionID,
            RMIN_OUTPUTMATERIALCODE to OutputMaterialCode,

            )
        val worker = getFgrnOneTimeRequestWorker(input, _index)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val msg = workInfo.outputData.getString(UIUtils.GRN_OUTPUT_DATA)
                            if (!msg.isNullOrEmpty()) {
                                offloadingItems[AppUtils.posExtension(workInfo.tags)].successMessage =
                                    msg
                                binding.rvReceivingOffline.adapter?.notifyItemChanged(
                                    AppUtils.posExtension(
                                        workInfo.tags
                                    )
                                )
                            }
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                            showLoading()
                        }
                    }
                }
            })
        if (_index == this.offloadingItems.size - 1) {
            changeBtn(item)
        }
    }

    private fun startRMINSync(item: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]
        //grnList1.forEachIndexed { _index, _element ->
        val input = workDataOf(FGRN_DATA to _element.fgrnTempId)
        val worker = getRminOneTimeRequestWorker(input, _index)
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
                        }
                        WorkInfo.State.FAILED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                            showLoading()
                        }
                    }
                }
            })
        if (_index == this.offloadingItems.size - 1) {
            changeBtn(item)
        }
    }

    fun changeBtn(item: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>) {
        hideLoading()
        val count = offloadingItems.filter { it.status!! == 4 }
        if (offloadingItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (offloadingItems.any { it.status!! == 3 })
            binding.btnSyncProceed.text = "ReSync"
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
