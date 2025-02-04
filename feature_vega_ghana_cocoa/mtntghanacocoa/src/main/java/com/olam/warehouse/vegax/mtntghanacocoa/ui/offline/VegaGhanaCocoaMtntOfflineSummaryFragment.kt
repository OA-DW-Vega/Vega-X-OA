package com.olam.warehouse.vegax.mtntghanacocoa.ui.offline

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
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchLotsMerge
import com.olam.warehouse.master.work.getGhanaCocoaMTNTDispatchOneTimeRequestWorker
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.DISPATCH_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaMtntOfflineSummaryBinding
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntghanacocoa.utils.ADD_LOT
import com.olam.warehouse.vegax.mtntghanacocoa.utils.getMTNTPlantName
import kotlinx.android.synthetic.main.item_vega_ghana_cocoa_mtnt_offline_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaCocoaMtntOfflineSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_ghana_cocoa_mtnt_offline_summary
    private val vm: VegaGhanaCocoaMtntViewModel by viewModel()

    private lateinit var binding: FragmentGhanaCocoaMtntOfflineSummaryBinding
    private var dispatchItems = arrayListOf<VegaCocoaDispatchWB>()
    private var callBack: CallBack? = null
    private var mergedLotsMap = mutableMapOf<Int, ArrayList<VegaEcuadorDispatchLots>>()
    private var deliveryDetails = ArrayList<VegaEcuadorDispatchLotsMerge>()
    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var dispatchLots = mutableListOf<VegaGhanaCocoaDispatchLots>()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var offlineStock = ArrayList<VegaEcuadorDispatchStocks>()
    private var purchaseOrder = ArrayList<VegaGhanaMtntPurchaseOrders>()

    companion object {
        fun newInstance() = VegaGhanaCocoaMtntOfflineSummaryFragment().putArgs {
        }
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            dispatchData: VegaCocoaDispatchWB,
            data: Any
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGhanaCocoaMtntOfflineSummaryBinding.inflate(layoutInflater)
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

        vm.purchaseOrderOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })

        vm.getPurchaseOrderOffline()
        vm.stocksOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateOfflineStock(it) })
        vm.fetchStocksOffline()
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })
        vm.getBagItems()

        vm.truck.observe(this, Observer {
            if (it != null) {
                updateUI(it)
            }

        })
        vm.getOfflineTruckDetails()

        /*vm.weighScaleWithLotMaterial.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getWeighScaleWithLotAndMaterial(
            vm.dispatchWh.plantId ?: "",
            vm.dispatchWh.purchaseDocNum ?: ""
        )*/
        /*vm.dispatchItemLocal.observe(this, Observer {\
            if (it != null) {
                updateUI(it)
            }
        })
        vm.getDispatchWithLineItem()*/

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
            if (btnText.contains("OK")) {
                vm.updateSyncedMtntDeletedItem(4)
                showDeleteMessage()
//                activity?.onBackPressed()
            } else showDialog()
        }
        vm.dispatchLots.observe(this, Observer {
            if (it != null && it.size > 0) {
                dispatchLots = it.toMutableList()
            }
        })
    }

    private fun updatePurchaseOrderOffline(data: List<VegaCocoaPurchaseOrders>?) {
        if (!data.isNullOrEmpty()) {
            var offlineData = data
//                .filter { it.openQuantity != "0.000" }
            offlineData.forEach {
                var vegaGhanaMtntPurchaseOrders = VegaGhanaMtntPurchaseOrders()
                vegaGhanaMtntPurchaseOrders.batchNumber = it.batchNumber
                vegaGhanaMtntPurchaseOrders.materialCode = it.materialCode
                vegaGhanaMtntPurchaseOrders.materialName = it.materialName
                vegaGhanaMtntPurchaseOrders.meins = it.meins
                vegaGhanaMtntPurchaseOrders.menge = it.menge
                vegaGhanaMtntPurchaseOrders.plantId = it.plantId
                vegaGhanaMtntPurchaseOrders.warehouseId = it.warehouseId
                vegaGhanaMtntPurchaseOrders.purchaseDocDesc = it.purchaseDocDesc
                vegaGhanaMtntPurchaseOrders.purchaseDocNum = it.purchaseDocNum
                vegaGhanaMtntPurchaseOrders.purchaseOrderType = it.purchaseOrderType
                vegaGhanaMtntPurchaseOrders.openQuantity = it.openQuantity

                purchaseOrder.add(vegaGhanaMtntPurchaseOrders)
            }

        }
    }


    private fun updateOfflineStock(offlineStocks: List<VegaEcuadorDispatchStocks>?) {
        offlineStock = offlineStocks as ArrayList<VegaEcuadorDispatchStocks>
    }

    private fun showDeleteMessage() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_mtnt)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.okconfirm),
                "",
                { activity?.finish() },
                { })
        }
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun showDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    startSync(dispatchItems.filter { it.status != 4 } as ArrayList<VegaCocoaDispatchWB>,
                        0)
                },
                { dismiss() })
        }
    }

    private fun updateUI(data: List<VegaCocoaDispatchWB>?) {
        data?.let { dispatch ->
            val dispatches = arrayListOf<VegaCocoaDispatchWB>()
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
//        hideLoading()
    }

    /*private fun updateUI(data: List<VegaEcuadorDispatchWithLineItems>?) {
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
    }*/

    private fun setUpAdapter(dispatchItemsList: ArrayList<VegaCocoaDispatchWB>) {
        dispatchItems = dispatchItemsList
        changeBtn()
        if (dispatchItems.size > 0) {
            binding.tvNoData.gone()
            binding.rvDispatchOffline.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvDispatchOffline.gone()
        }
        binding.rvDispatchOffline.setUp(
            dispatchItems,
            R.layout.item_vega_ghana_cocoa_mtnt_offline_summary,
            { it, pos ->
//                val dispatch = it.dispatch
                //tvStoNo.text = it.purchaseDocNum
                tvSDWaybillNo.text = it.sidingDepotwayBillNumber
                tvDispatchQty.text = it.purchaseQuantity
                tvDestinationWH.text =
                    it.storageLocationCode.plus("-").plus(it.plantName).plus("-").plus(
                        it.destinationWH
                    ).plus("-").plus(
                        it.destinationWHName
                    )
                tvDestinationWH.text = getMTNTPlantName(it.destinationWH!!, true).plus(" - ")
                    .plus(it.destinationWHName)
                /*it.recPlantId.plus("-").plus(it.recPlantName).plus("-").plus(
                    it.destinationWH
                ).plus("-").plus(
                    it.destinationWHName
                )*/

                tvDeliveryId.text = it.weighBridgeId

                if (it.delivery == "")
                    tvDeliveryId.text = it.weighBridgeId
                else
                    tvDeliveryId.text = it.delivery
                when (it.status) {
                    4 -> {
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
                    3 -> {
                        tvViewDetails.isEnabled = true
                        tvError.visible()
                        tvError.text = it.message
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
                    vm.getDispatchLotList(it.weighBridgeId)
                    showDeleteDialog(it)
                }
                tvViewDetails.setOnClickListener { view ->
                    if (it.status != 4) viewDetails(it)
//                    if (it.status != Status.MTNT_COMPLETED) viewDetails(it)
                }
            })
    }

    private fun viewDetails(it: VegaCocoaDispatchWB) {
        callBack?.replaceFragment(
            ADD_LOT,
            it,
            it.weighBridgeId  // vm.lotlist
        )
    }

    private fun showDeleteDialog(item: VegaCocoaDispatchWB) {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_msg)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    var summedWeight = 0.000
                    var sum = 0.000
                    var selectedCard =
                        dispatchItems.filter { it.weighBridgeId == item.weighBridgeId }
                    var po = purchaseOrder
                        .filter { it.purchaseDocNum == selectedCard[0].purchaseDocNum.toString() }
                        .filter { it.purchaseDocDesc == selectedCard[0].purchaseDocDesc.toString() }

                    dispatchItems.remove(item)
                    dispatchLots.forEach { item ->
                        var stock = offlineStock.filter { it.batchNumber == item.batchNumber }

                        var updatedWeight =
                            stock[0].weight.toString().toDouble() + (item.editedWeight.toString()
                                .toDouble())
                        vm.updateStockDetails(updatedWeight.toString(), item.batchNumber)

                        var updatedOpenQuantity =
                            po[0].openQuantity.toString().toDouble() + item.editedWeight.toString()
                                .toDouble()
                        vm.updateMtntPurchaseOrderDetails(
                            updatedOpenQuantity.toString(),
                            po[0].purchaseDocNum.toString()
                        )

                    }
//                vm.updateDeletedItem(item.weighBridgeId)
                    vm.deleteTruckAndLotsData(item.weighBridgeId)
                },
                { dismiss() })
        }
    }

    private fun changeBtn() {
        hideLoading()

        val count = dispatchItems.filter { it.status == 4 }

        if (dispatchItems.size == count.size)
            binding.btnSyncProceed.text = getString(R.string.ok)
        else if (dispatchItems.any { it.status == 3 })
            binding.btnSyncProceed.text = getString(R.string.resync)
        else
            binding.btnSyncProceed.text = getString(com.olam.warehouse.presentation.R.string.sync)
    }

    var syncCount = 0
    private fun startSync(item: ArrayList<VegaCocoaDispatchWB>, _index: Int) {
        PreferenceHelper.save(Constants.START_SYNC, true)
        val _element = item[_index]

        val input = workDataOf(UIUtils.DISPATCH_DATA to _element.weighBridgeId)
        val worker = getGhanaCocoaMTNTDispatchOneTimeRequestWorker(input, _index)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(this, Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
//                                    hideLoading()
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                            if (_index == this.dispatchItems.size - 1) {
                                changeBtn()
                            }
                        }
                        WorkInfo.State.FAILED -> {
//                                    hideLoading()
                            val position = AppUtils.posExtension(workInfo.tags) + 1
                            syncCount++
                            if (syncCount != item.size) startSync(item, position)
                            if (syncCount == item.size) {
                                syncCount = 0
                                PreferenceHelper.save(Constants.START_SYNC, false)
                            }
                            this.dispatchItems[_index].message =
                                workInfo.outputData.getString(DISPATCH_OUTPUT_DATA)
                            if (_index == this.dispatchItems.size - 1) {
                                changeBtn()
                            }
                        }
                        WorkInfo.State.RUNNING -> showLoading()
                    }
                }
            })
//                hideLoading()

        if (_index == (item.size - 1)) {
//            hideLoading()
        }

    }

}
