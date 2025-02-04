package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.offline

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
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.master.work.getGhanaCocoaOffloadingOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.PrintPreviewDialogFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.WAREHOUSE_NUMBER
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.ActivityVegaOffloadingGhanaCocoaOfflineSummaryBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.OFFLOADING_SUMMARY_FRAG
import kotlinx.android.synthetic.main.item_vaga_offloading_ghana_cocoa_offline_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaGhanaCocoaOffloadingOfflineSummary : BaseFragment() {
    override val layoutResourceId = R.layout.activity_vega_offloading_ghana_cocoa_offline_summary
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private lateinit var binding: ActivityVegaOffloadingGhanaCocoaOfflineSummaryBinding
    private var offloadingItems = arrayListOf<VegaEcuaOffloadingWithLineItems>()
    private var callBack: CallBack? = null
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var grnPrice: String = ""
    private var materialList = mutableListOf<VegaMaterial>()
    private var uomDetails = ArrayList<VegaUomDetails>()

    companion object {
        fun newInstance() = VegaGhanaCocoaOffloadingOfflineSummary().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>,
            bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>,
            grnPrice: String,
            isOffline:Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityVegaOffloadingGhanaCocoaOfflineSummaryBinding.inflate(layoutInflater)
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
        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()
        vm.offloadingItemLocal.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getOffloadingWithLineItem()

        vm.product.observe(viewLifecycleOwner, Observer {
            /* it.forEach { item ->
                         it.forEach { material ->
                             if (material.materialName == tvProduct.text.toString()) {
                                 grnPrice = material.price.toString()
                             }
                         }
             }*/

            materialList = it as MutableList<VegaMaterial>
        })
        vm.getProducts()


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
            if (btnText.contains("OK")){
                vm.deleteOffloadingItem()
                showDeleteMessage()
            } else showDialog()
        }
    }

    private fun showDeleteMessage() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_offloading)
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
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    startSync(offloadingItems, 0)
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
        binding.rvReceivingOffline.setUp(
            offloadingItems,
            R.layout.item_vaga_offloading_ghana_cocoa_offline_summary,
            { it, pos ->
                val receiving = it.receiving
                tvNoOfBags.text = receiving.bagCount
                tvWeight.text = receiving.netWeight.plus(" ").plus("KG")
                tvProductType.text = receiving.supplierName
                tvWbId.text = receiving.grnNumber
                //viewStatus.isVisible = receiving.isSynced
                when (receiving.status) {
                    Status.RECEVING_COMPLETED -> {
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
                    Status.SYNC_ERROR -> {
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
                    showDeleteDialog(it)
                }
                tvViewDetails.setOnClickListener { view ->
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
        grnPrice =materialList.filter { data.receiving.materialCode.toString().contains(it.materialCode)}.map { it.price }.single().toString()
        callBack?.replaceFragment(
            OFFLOADING_SUMMARY_FRAG,
            data.receiving,
            mReceiving as ArrayList<VegaReceiving>,
            data.lineItems as ArrayList<VegaEcuadorOffloadingBagMaterial>,
            grnPrice,true
        )
    }

    private fun showDeleteDialog(item: VegaEcuaOffloadingWithLineItems) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    offloadingItems.remove(item)
                    vm.updateDeletedItem(item.receiving.tmpWbId)
                },
                { dismiss() })
        }
    }

    var syncCount = 0
    private fun startSync(item: ArrayList<VegaEcuaOffloadingWithLineItems>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]
        //grnList1.forEachIndexed { _index, _element ->
        val input = workDataOf(
            RECEIVING_DATA to _element.receiving.tmpWbId,
            WAREHOUSE_NUMBER to _element.receiving.whReceiptNum
        )
        val worker = getGhanaCocoaOffloadingOneTimeRequestWorker(input, _index)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            hideLoading()
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                            if (_index == this.offloadingItems.size - 1) {
                                changeBtn(item)
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            hideLoading()
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                            if (_index == this.offloadingItems.size - 1) {
                                changeBtn(item)
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                            showLoading()
                        }
                    }
                }
            })
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
