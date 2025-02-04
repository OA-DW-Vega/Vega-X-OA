package com.olam.warehouse.vegax.processingghana.ui.rmin.offline

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
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminItems
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminLots
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaOfflineRmin
import com.olam.warehouse.master.work.getOffloadingOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.ActivityVegaGhanaProcessingRminOfflineSummaryBinding
import com.olam.warehouse.vegax.processingghana.ui.rmin.VegaGhanaRminViewModel
import com.olam.warehouse.vegax.processingghana.utils.OFFLINE_RMIN_SUMMARY_FRAG
import kotlinx.android.synthetic.main.item_vaga_ghana_processing_fgrn_offline_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */
class VegaGhanaProcessingRminOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.activity_vega_ghana_processing_rmin_offline_summary
    private val vm: VegaGhanaRminViewModel by viewModel()
    private lateinit var binding: ActivityVegaGhanaProcessingRminOfflineSummaryBinding
    private var offloadingItems = arrayListOf<VegaGhanaOfflineRminProcessLotDetails>()
    private var callBack: CallBack? = null
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var rminLots = mutableListOf<VegaGhanaOfflineRminLots>()
    private var rminItems = mutableListOf<VegaGhanaOfflineRminItems>()
    private var stockList = VegaEcuadorDispatchStocks()

    companion object {
        fun newInstance() = VegaGhanaProcessingRminOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            rminLots: List<VegaGhanaOfflineRminLots>,
            rminItems: List<VegaGhanaOfflineRminItems>,
            poNo: String, stage: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityVegaGhanaProcessingRminOfflineSummaryBinding.inflate(layoutInflater)
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
        vm.offlineRminItemLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getOfflineRminItem()

        vm.stockLot.observe(this, Observer {
            stockList = it
        })

        vm.offlineRminLotsLocal.observe(this, Observer {
            rminLots = it as MutableList<VegaGhanaOfflineRminLots>
        })
        vm.getOfflineRminLots()

        vm.offlineRminItemsLocal.observe(this, Observer {
            rminItems = it as MutableList<VegaGhanaOfflineRminItems>
        })
        vm.getOfflineRminItems()
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
            binding.btnSyncProceed.isEnabled = true
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

    private fun updateUI(data: List<VegaGhanaOfflineRmin>?) {
        data?.let { receiving ->
            val offline = arrayListOf<VegaGhanaOfflineRminProcessLotDetails>()
            when {
                receiving.isNotEmpty() -> {
                    receiving.forEach {

                        offline.addAll(it.rminLot)
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
            "Confirm FGRN"
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    startSync(offloadingItems)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(offloadingItems1: ArrayList<VegaGhanaOfflineRminProcessLotDetails>) {
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
            R.layout.item_vaga_ghana_processing_rmin_offline_summary,
            { it, pos ->
                val receiving = it
                tvNoOfBags.text = receiving.poNo
                tvWeight.text = receiving.netWeight.plus(" ").plus("MT")
                tvProductType.text = receiving.materialName
                tvWbId.text = receiving.rminTempId
                receiving.status = 2
                //viewStatus.isVisible = receiving.isSynced
                when (receiving.status) {
                    1 -> {
                        ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.do_icon_completed))
                        viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(4)
                            )
                        )
                        tvError.gone()
                        tvViewDetails.text = "Success"
                        tvViewDetails.isEnabled = false
                    }
                    0 -> {
                        tvViewDetails.isEnabled = true
                        tvError.visible()
                        tvError.text = receiving.syncStatusMsg
                        ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.ic_do_icon_error))
                        viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(3)
                            )
                        )
                    }
                    else -> {
                        ivStatus.setImageDrawable(context.getDrawable(com.olam.warehouse.presentation.R.drawable.icon_no_progresss))
                        viewStatus.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                UIUtils.getSyncItemBackgroundColor(1)
                            )
                        )
                        tvError.gone()
                        tvViewDetails.isEnabled = true
                    }
                }
                ivDelete.setOnClickListener { view ->
                    vm.getStockDetailsByBatchNo(it.batchNumber!!)
                    if (receiving.fgrnStatus!!)
                        showAlert()
                    else
                        showDeleteDialog(it)
                }
                tvViewDetails.setOnClickListener { view ->
                    ViewDetails(it.rminTempId, it.poNo!!, it.stage!!)
//                    else {
//                        val tallyKeys = ArrayList<String>()
//                        tallyKeys.add(it.receiving.encodedImageContent ?: "")
//                        showPreviewDialog(tallyKeys)
//                    }
                }
            })
    }

    private fun ViewDetails(data: String, poNo: String, stage: String) {
        var lotList = rminLots.filter { it.rminTempId == data }
        var itemList = rminItems.filter { it.rminTempId == data }
        callBack?.replaceFragment(
            OFFLINE_RMIN_SUMMARY_FRAG,
            lotList,
            itemList, poNo, stage
        )
    }

    private fun showAlert() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_rmin)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.okconfirm),
                    isPositive = true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun showDeleteDialog(item: VegaGhanaOfflineRminProcessLotDetails) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    var updatedWeight =
                        stockList.weight?.toDouble()!! + item.netWeight?.toDouble()!!
                    offloadingItems.remove(item)
                    vm.updateDeletedItem(item.rminTempId)
                    vm.updateStockDetails(updatedWeight.toString(), item.batchNumber!!)
                },
                { dismiss() })
        }
    }

    private fun startSync(item: ArrayList<VegaGhanaOfflineRminProcessLotDetails>) {
        val gson = Gson()
        item.forEachIndexed { _index, _element ->
            if (!_element.status!!.equals(1)) {
                val input = workDataOf(RECEIVING_DATA to _element.rminTempId)
                val worker = getOffloadingOneTimeRequestWorker(input, _index)
                enQueueWorker(worker, requireContext())
                WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                    .observe(this, Observer { workInfo ->
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

    fun changeBtn(item: ArrayList<VegaGhanaOfflineRminProcessLotDetails>) {
        val count = offloadingItems.filter { it.status!!.equals(Status.RECEVING_COMPLETED) }
        if (offloadingItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (offloadingItems.any { it.status!!.equals(Status.SYNC_ERROR) })
            binding.btnSyncProceed.text = "OK"
        else
            binding.btnSyncProceed.text = "OK"
    }

    private fun showPreviewDialog(tallyPrintKeys: ArrayList<String>) {
        val list = mutableListOf<String>()
        list.addAll(tallyPrintKeys)
        val dialogFragment = PrintPreviewDialogFragment(list)
        activity?.supportFragmentManager?.let { it1 -> dialogFragment.show(it1, "signature") }
    }

}
