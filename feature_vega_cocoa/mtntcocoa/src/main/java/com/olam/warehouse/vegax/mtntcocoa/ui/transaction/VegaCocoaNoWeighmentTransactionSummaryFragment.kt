package com.olam.warehouse.vegax.mtntcocoa.ui.transaction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.vega.model.VegaCocoaNoWeighmentWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentVegaCocoaNwSummaryBinding
import com.olam.warehouse.vegax.mtntcocoa.databinding.ItemVegaCocoaNoWeighmentLotBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaNoWeighmentTransactionSummaryFragment : BaseFragment() {

    private var dispatchLotsList = mutableListOf<VegaCocoaNoWeighmentLot>()
    override val layoutResourceId: Int = R.layout.fragment_vega_cocoa_nw_summary
    private lateinit var binding: FragmentVegaCocoaNwSummaryBinding
    private var callBack: CallBack? = null
    private var summaryObj: VegaCocoaNoWeighmentModel? = null
    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
  private  var isWeighscale:Boolean=false

    companion object {
        fun newInstance(data: VegaCocoaNoWeighmentModel) = VegaCocoaNoWeighmentTransactionSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCocoaNwSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("/mtntcocoa/ui/transaction/VegaCocoaNoWeighmentTransactionSummaryFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
        binding.tvTruckValue.text = summaryObj?.vehicleNumber
        binding.tvDestValue.text =
            summaryObj?.warehouseId.plus("-").plus(summaryObj?.plantName)
        binding.tvDateValue.text = summaryObj?.vehicleNumber
        binding.tvDriverNameValue.text = summaryObj?.driverName
        binding.tvStoNoValue.text =
            summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
        binding.tvMaterialName.text = summaryObj?.materialName
        binding.tvStoWeightValue.text =
            summaryObj?.netWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                .plus(summaryObj?.unitsOfMeasure)
        binding.btProceed.text = getString(com.olam.warehouse.presentation.R.string.ok)
        binding.btProceed.setOnClickListener { activity?.onBackPressed() }
        val decodedString: ByteArray = Base64.decode(summaryObj?.imageString, Base64.DEFAULT)
        val decodedByte: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        binding.ivTicket.setImageBitmap(decodedByte)
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager
        vm.weighScaleWithLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchNoWeighmentWh = it.dispatch
                vm.noWeighmentlots.clear()
                vm.noWeighmentlots.addAll(it.lineItems)
                updateLocalDbData(it)
            }
        })
        vm.getWeighScaleWithLot(
            summaryObj?.weighBridgeId ?: ""
        )
    }

    private fun updateLocalDbData(weighBridge: VegaCocoaNoWeighmentWithLots) {
        dispatchLotsList.clear()
        setUpAdapter(weighBridge.lineItems as ArrayList<VegaCocoaNoWeighmentLot>)
        dispatchLotsList.addAll(weighBridge.lineItems)
        //binding.tvDestValue.text = weighBridge.lineItems[0].delivery
        vm.bagItems.observe(viewLifecycleOwner, Observer { getBagList(it) })
        vm.getBagItems(isWeighscale,weighBridge.lineItems[0].batchNumber, weighBridge.lineItems[0].weighBridgeId)
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        /*val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }*/
        bagList.addAll(bagItems)
    }


    private fun setUpAdapter(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        val lots = list
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_vega_cocoa_no_weighment_lot,
            ItemVegaCocoaNoWeighmentLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batchNumber
                bindItem.cbEndLot.visibility = View.GONE
                bindItem.tvEndLot.visibility = View.GONE
                if (item.isEndLot == true) {
                    bindItem.tvStorageLoss.visibility = View.VISIBLE
                    bindItem.tvStorageLossValue.visibility = View.VISIBLE
                } else {
                    bindItem.tvStorageLoss.visibility = View.GONE
                    bindItem.tvStorageLossValue.visibility = View.GONE
                }
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
                bindItem.tvScaleGradeValue.text = item.materialName
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvDispatchUOMValue.text = item.weightToDispatchUOM
                bindItem.ivScaleClose.visibility = View.GONE
                bindItem.tvAddWeight.visibility = View.GONE
                bindItem.tvVendor.visibility =
                    if (summaryObj?.isThirdPartyMaterial == true) View.VISIBLE else View.GONE
                bindItem.tvVendorValue.visibility =
                    if (summaryObj?.isThirdPartyMaterial == true) View.VISIBLE else View.GONE
                bindItem.tvVendorValue.text = item.vendorName
                val lossOrGain = calculateLoss(
                    item.editedWeight ?: "", item.weight ?: "", item.unitOfMeasure ?: "",
                    item.weightToDispatchUOM ?: ""
                )
                bindItem.tvStorageLossValue.text = lossOrGain.plus(
                    " "
                ).plus(item.weightToDispatchUOM)
                bindItem.tvStorageLoss.text =
                    (if (lossOrGain.toDouble() < 0) getString(R.string.storage_gain) else getString(
                        R.string.storage_loss
                    ))
            })
    }

    private fun calculateLoss(editedWeight: String, weight: String, uom: String, wUom: String): String {
        var loss = ""
        if (uom == wUom) {
            loss = weight.toDouble().minus(editedWeight.toDouble()).toString()
        } else if (uom == "MT" && wUom == "KG") {
            loss = (weight.toDouble() * 1000).minus(editedWeight.toDouble()).toString()
        }
        return loss
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    /*private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    var docNo = listOf<String>()
                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }
                            ?: listOf()
                    moveToSuccessPage(prepareSuccessMessage(response.data?.data), docNo)
                    vm.updateVirtualSuccessStatus(vm.dispatchNoWeighmentWh, vm.noWeighmentlots)
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                    val success = response.data?.data ?: ArrayList()
                    for (item in success) {
                        dispatchLotsList.single { it.batchNumber == item.batchNumber }.apply {
                            weighScaleWbId = item.weighBridgeId
                            pickingFlag = item.pickingFlag
                            deliveryFlag = item.deliveryFlag
                            deliveryItem = item.deliveryItem
                            delivery = item.delivery
                            isPgiFlag = item.pgiFlag
                            storageLossFlag = item.storageLossFlag
                        }
                    }
                    vm.noWeighmentlots.clear()
                    vm.dispatchNoWeighmentWh = summaryObj!!
                    vm.noWeighmentlots.addAll(dispatchLotsList)
                    vm.saveNoWeighmentDetails()
                    vm.addLoTInDB(dispatchLotsList)
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaCocoaVirtualDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message =
                    it.weighBridgeId.plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun moveToSuccessOffline() {
        val deliveryId = deliveryList.map { it.batchNumber }.toString().replace("{", "").replace("}", "")
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra("fromcoffee", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.mtnr_ws_success, summaryObj?.weighBridgeId).plus(
                getString(
                    R.string.mtnr_ws_success_docu_no,
                    deliveryId
                )
            )
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun moveToSuccessPage(deliveryId: String?, docNo: List<String>) {
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra("fromcoffee", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
        if (AppUtils.isOnline()) intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch)
        ) else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch_offline)
        )
        *//*intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))*//*
        if (!documentNo.isEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.mtnr_ws_success, deliveryId).plus(
                    getString(
                        R.string.mtnr_ws_success_docu_no,
                        documentNo
                    )
                )
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.mtnr_ws_success, deliveryId))
        startActivity(intent)
        requireActivity().finish()
    }*/

    /*private fun prepareLotCard(): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        dispatchLotsList.forEach {
            val lot = VegaCoffeeSalesLots()
            lot.batchNumber = it.batchNumber
            lot.editedWeight = it.editedWeight
            lot.unitOfMeasure = it.unitOfMeasure
            lot.materialName = it.materialName
            lot.materialCode = it.materialCode
            lot.storageLocationCode = it.storageLocationCode
            lotList.add(lot)
        }
        return lotList
    }*/

    /*private fun prepareDeliveryList() {
        deliveryList.clear()
        val list = ArrayList<VegaCocoaVirtualDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCocoaVirtualDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.createdDate = summaryObj?.erdat
            deliveryDetail.purchaseDocNum = summaryObj?.purchaseDocNum
            deliveryDetail.purchaseDocDesc = summaryObj?.purchaseDocDesc
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = summaryObj?.startTime
            deliveryDetail.endTime = summaryObj?.endTime
            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.pgiFlag = item.isPgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            *//*var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            *//**//*var startTime = ""
            var endTime = ""*//**//*
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                *//**//*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*//**//*
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            }*//*
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            bagItems.forEach { item1 ->
                grossWeight = item1.grossWeight.toDouble()*//*grossWeight.plus(item1.grossWeight.toDouble())*//*
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            }
            deliveryDetail.grossWeight = if (deliveryDetail.unitsOfMeasure.equals("MT")) convertKgToMT(
                grossWeight.toString().trim()
            ) else grossWeight.toString().trim()//
            deliveryDetail.tareWeight =
                deliveryDetail.grossWeight?.toDouble()?.minus(deliveryDetail.netWeight!!.toDouble()).toString()
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
            } else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            list.add(deliveryDetail)
        }
        deliveryList.addAll(list)
    }*/

}
