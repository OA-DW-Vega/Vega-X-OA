package com.olam.warehouse.vegax.offloadingcocoa.ui.transaction

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.enQueueWorker
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcocoa.R
import com.olam.warehouse.vegax.offloadingcocoa.databinding.FragmentVegaCocoaOffloadTransactionBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemVegaCocoaOffloadTrasactionLayoutBinding
import com.olam.warehouse.vegax.offloadingcocoa.databinding.ItemVegaCocoaOffloadVirtualPendingListBinding
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcocoa.ui.VegaCoCoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcocoa.utils.TRANSACTION_SUMMARY
import com.olam.warehouse.vegax.offloadingcocoa.work.getMtnrOneTimeRequestWorker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.abs

class VegaCocoaMtnrTransactionFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_cocoa_offload_transaction
    private lateinit var binding: FragmentVegaCocoaOffloadTransactionBinding
    private val vm: VegaCoCoaOffloadingViewModel by viewModel()
    private var transactionList = mutableListOf<VegaCoCoaReceivingMtnrWithLots>()
    private var transactionSearchList = mutableListOf<VegaCoCoaReceivingMtnrWithLots>()
    private var pendingList = mutableListOf<VegaCoCoaReceivingMtnrWithLots>()
    private var callBack: VegaCoCoaOffloadReplaceFragmentCallback? = null
    private var priceEdit = false
    private var isPendingClicked = true

    companion object {
        fun newInstance() = VegaCocoaMtnrTransactionFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoCoaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCocoaOffloadTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingcocoa/ui/transaction/VegaCocoaMtnrTransactionFragment")
            .title("Transaction CoCoa")
            .with(tracker)
    }

    private fun initUI() {
        vm.offlineMtnr.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                transactionList.clear()
                transactionList.addAll(it)
                binding.rvTransaction.removeAllViews()
                if (!isPendingClicked)
                    binding.tvHistory.performClick()
            }
        })
        vm.offlinePendingMtnr.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                pendingList.clear()
                pendingList.addAll(it)
                binding.rvTransaction.removeAllViews()
                if (isPendingClicked)
                    binding.tvPending.performClick()
            }

        })

        vm.getPendingList()
        vm.getTransactions()

        binding.tvPending.setOnClickListener {
            isPendingClicked = true
            moveToPending()
        }
        binding.tvHistory.setOnClickListener {
            isPendingClicked = false
            moveToHistory()
        }
        binding.tvSortByDate.setOnClickListener { sortByDate() }
        binding.tvSync.setOnClickListener { showConfirmDialog() }

        binding.etSearchVendor.onChange {
            if (it.isNotEmpty()) {
                transactionSearchList.clear()
                transactionList.filter { it.receiving.status.equals(Status.SYNC_COMPLETED) }.forEach { item ->
                    if (item.receiving.supplierName?.contains(
                            it,
                            true
                        ) == true || item.receiving.supplierCode?.contains(
                            it,
                            true
                        ) == true
                    ) transactionSearchList.add(item)
                }
                setupAdapter(transactionSearchList)
            } else {
                setupAdapter(transactionList.filter { it.receiving.status.equals(Status.SYNC_COMPLETED) }
                    .toMutableList())
            }
        }
        EnableSync(AppUtils.isOnline())
    }

    private fun moveSummary(qualityItems: List<VegaQuality>) {
        /*if (!priceEdit)
            callBack?.replaceFragment(FRAG_SUMMARY, selectedReceiving, prepareQualityParamData(qualityItems))
        else {
            if (selectedReceiving.grnType?.contains("tolling", true) == true)
                callBack?.replaceFragment(GRN_QUALITY, selectedReceiving)
            else if (selectedReceiving.grnType?.contains("ptbf", true) == true)
                callBack?.replaceFragment(FRAG_PTBF_PRICING, selectedReceiving, prepareQualityParamData(qualityItems))
            else
                callBack?.replaceFragment(FRAG_PRICING, selectedReceiving, prepareQualityParamData(qualityItems))
        }*/
    }

    private fun updateUI(data: List<VegaReceiving>?) {
        moveToPending()
    }

    private fun moveToHistory() {
        binding.etSearchVendor.visible()
        binding.tvSortByDate.visible()
        binding.tvSync.gone()
        binding.tvHistory.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvPending.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        setupAdapter(transactionList)
    }

    private fun moveToPending() {
        binding.etSearchVendor.gone()
        binding.tvSortByDate.gone()
        binding.tvSync.visible()
        binding.tvPending.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvHistory.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvHistory.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        setupPendingAdapter(pendingList)

        // setupAdapter(grnList.filter { it.status.equals(Status.SYNC_PENDING) }.toMutableList())
    }

    private fun sortByDate() {
        if (transactionList.isNotEmpty()) {
            val data = transactionList
            transactionList = data.asReversed()
            setupAdapter(transactionList)
        }
    }

    private fun setupAdapter(itemList: List<VegaCoCoaReceivingMtnrWithLots>) {
        if (itemList.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }

        binding.rvTransaction.setUpAdapter(
            itemList as MutableList,
            R.layout.item_vega_cocoa_offload_trasaction_layout,
            ItemVegaCocoaOffloadTrasactionLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvObdNoValue.text = it.receiving.delivery
                bindItem.tvDateValue.text = it.receiving.erdat
                bindItem.tvMaterialValue.text = it.receiving.materialName
                bindItem.tvTruckNoValue.text = it.receiving.vehicleNumber
                bindItem.tvWeightValue.text = it.receiving.netWeight
                bindItem.tvTempGrnValue.text = it.receiving.tempGrnNumber
                if (it.receiving.transitLossDocNo.isNullOrEmpty()) {
                    bindItem.tvTransitLoss.visibility = View.GONE
                    bindItem.tvTransitLossValue.visibility = View.GONE
                } else
                    bindItem.tvTransitLossValue.text = it.receiving.transitLossDocNo
                bindItem.tvSendingWHValue.text =
                    it.receiving.storageLocationCode.plus(" - ")
                        .plus(it.receiving.storageLocationName)
                bindItem.clMain.setOnClickListener { moveToDetails(itemList[pos].receiving) }
            })
    }

    private fun setupPendingAdapter(itemList: List<VegaCoCoaReceivingMtnrWithLots>) {
        if (itemList.isNotEmpty()) {
            binding.rvTransaction.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvTransaction.gone()
            binding.tvNoData.visible()
        }

        binding.rvTransaction.setUpAdapter(
            itemList as MutableList,
            R.layout.item_vega_cocoa_offload_virtual_pending_list,
            ItemVegaCocoaOffloadVirtualPendingListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvObdNoValueP.text = it.receiving.delivery
                bindItem.tvVendorValueP.text = abs(it.receiving.tempGrnNumber!!.toLong()).toString()
                bindItem.tvDateValueP.text = it.receiving.erdat
                bindItem.tvMaterialValueP.text = it.receiving.materialName
                bindItem.tvSendingWHValuePend.text = it.receiving.supplierName
                bindItem.tvWeightValueP.text = it.receiving.netWeight
                bindItem.tvError.text = it.receiving.syncStatusMsg
                if (it.receiving.syncStatusMsg.isNullOrEmpty()) {
                    bindItem.tvShowMore.gone()
                    bindItem.tvError.gone()
                    bindItem.tv.gone()
                } else {
                    bindItem.tvShowMore.visible()
                    bindItem.tvError.visible()
                    bindItem.tv.visible()
                }
                //tvWeightValue.text = it.receiving.
                bindItem.tvShowMore.setOnClickListener { view -> showErrorDialog(it.receiving.syncStatusMsg.toString()) }
                bindItem.tvSendingWHValueP.text =
                    it.receiving.storageLocationCode.plus(" - ")
                        .plus(it.receiving.storageLocationName)
                bindItem.clMainP.setOnClickListener { moveToDetails(itemList[pos].receiving) }
                bindItem.ivCloseP.setOnClickListener {
                    showItemDeleteDialog(
                        itemList[pos].receiving.tempGrnNumber!!,
                        itemList[pos].receiving.mtnCode!!,
                        pos
                    )
                }
            })
    }


    private fun moveToDetails(id: VegaCoCoaReceiving) {
        callBack?.replaceFragment(TRANSACTION_SUMMARY, id)
    }
    /*private fun moveEdit(it: VegaReceiving) {
        if(it.wbFlag==true && it.qcFlag==false && it.grnFlag==false)
            callBack?.replaceFragment(GRN_QUALITY, it)
        else if(it.wbFlag==true && it.qcFlag==true && it.grnFlag==false) {
            priceEdit = true
            selectedReceiving = it
            vm.getQuality(it.tmpWbId)
        }else
            callBack?.replaceFragment(GRN_SPOT, it)

    }*/

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.sync_all)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    startSync(pendingList)
                },
                { dismiss() })
        }
    }

    private fun showItemDeleteDialog(temp: String, mtnNumber: String, pos: Int) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.deleteStatus(temp, mtnNumber)
                    pendingList.removeAt(pos)
                    binding.rvTransaction.adapter?.notifyDataSetChanged()
                },
                { dismiss() })
        }
    }


    private fun showErrorDialog(title: String) {
        MaterialDialog(requireContext()).show {
            message(null, title)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }

    private fun EnableSync(flag: Boolean) {
        binding.tvSync.isEnabled = flag
        if (flag)
            ViewCompat.setBackgroundTintList(
                binding.tvSync,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                }
            )
        else
            ViewCompat.setBackgroundTintList(
                binding.tvSync,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.grey
                    )
                }
            )
    }

    private fun startSync(grnList1: MutableList<VegaCoCoaReceivingMtnrWithLots>) {
        grnList1.forEachIndexed { _index, _element ->
            val input = workDataOf(UIUtils.RECEIVING_DATA to _element.receiving.tempGrnNumber)
            val worker = getMtnrOneTimeRequestWorker(input, _index)
            enQueueWorker(worker, requireContext())
            WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
                .observe(viewLifecycleOwner, Observer { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.SUCCEEDED -> hideLoading()
                            WorkInfo.State.FAILED -> hideLoading()
                            WorkInfo.State.RUNNING -> showLoading()
                            else -> {

                            }
                        }
                    }
                })
            if (_index == grnList1.size - 1) {
                hideLoading()
                initUI()
            }
        }
    }

    fun getBack() {
        vm.getPendingList()
        vm.getTransactions()
    }

}

class OverlapDecoration : RecyclerView.ItemDecoration() {
    var vertOverlap = -3
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(vertOverlap, 0, 0, 0)
    }

}
