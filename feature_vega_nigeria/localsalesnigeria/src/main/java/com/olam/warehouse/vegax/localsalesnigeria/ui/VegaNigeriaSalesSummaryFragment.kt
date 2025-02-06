package com.olam.warehouse.vegax.localsalesnigeria.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesLotWithbags
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalesnigeria.R
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaLocalSalesAssignLot
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesDeliveryDetail
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesPostRequest
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalesnigeria.databinding.FragmentNigeriaSalesSummaryBinding
import com.olam.warehouse.vegax.localsalesnigeria.databinding.ItemLotNigeriaSalesSummaryBinding
import com.olam.warehouse.vegax.localsalesnigeria.databinding.ItemNigeriaSalesMaterialListBinding
import com.olam.warehouse.vegax.localsalesnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 8/21/2020.
 */
class VegaNigeriaSalesSummaryFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_nigeria_sales_summary
    private val vm: VegaNigeriaSalesViewModel by viewModel()
    private lateinit var binding: FragmentNigeriaSalesSummaryBinding
    private var materialList = ArrayList<materialList>()
    private var bagList = arrayListOf<VegaCoffeeSalesBagMaterial>()
    private var salesOrder = VegaCoffeeSalesOrder()
    private var callBack: CallBack? = null
    private var materialUOM: String = ""
    private var mergedBatchNumbers: List<String?> = ArrayList<String>()
    private var assignLotList= ArrayList<VegaNigeriaLocalSalesAssignLot>()
    private var selectedStorageLocation: String = ""
    private var isEmptyMergedId:Boolean = false


    interface CallBack {
        fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots)
        fun navigateToAssignLot(
            salesOrder: VegaCoffeeSalesOrder,
            materialList: ArrayList<materialList>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(
            model: VegaCoffeeSalesOrder,
            materialList: ArrayList<materialList>
        ) = VegaNigeriaSalesSummaryFragment().putArgs {
            putParcelable(SALES_ITEM, model)
            putParcelableArrayList(MATERIAL_LIST, materialList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaSalesSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        // vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalesNigeria/ui/VegaNigeriaSalesSummaryFragment")
            .title("Vega_Nigeria/Local Sales").with(tracker)
    }
    private fun initExtra() {
//        binding.btAssignLot.visibility = View.GONE
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()
        salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALES_ITEM) ?: VegaCoffeeSalesOrder()
        vm.salesOrder = salesOrder
        setUpMaterialAdapter(materialList)
        binding.tvDestValue.text = salesOrder.saleOrderId
        binding.tvDateValue.text = salesOrder.customerName
        if (salesOrder.remarks.isNullOrEmpty()) {
            binding.cvRemark.gone()
            binding.tvRemarks.gone()
        } else {
            binding.cvRemark.visible()
            binding.tvRemarks.visible()
        }
        binding.tvRemark.text = salesOrder.remarks

        var mergedids = ArrayList<String>()
        vm.salesOrder.lotList.forEach {
            if(it.mergedLotId == "")
                isEmptyMergedId = true
            else
                mergedids.add(it.mergedLotId.toString())
        }
        if(!isEmptyMergedId)
        {
            if(vm.salesOrder.lotList.size > 1)
                binding.tvAssignLot.text = "LOT IDs: ".plus(mergedids.distinct().toString().replace("[","").replace("]",""))
        }
    }

    private fun initUI() {
        vm.dispatchSalesItem.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateSalesItem(it) else updateSalesItem(VegaCoffeeSalesOrderWithLots()) })
        vm.getDispatchSalesItem(salesOrder.saleOrderId, salesOrder.salesType, salesOrder.salesTempId)
        binding.ivEditRemark.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener {
            validateProceed()
//            showConformationDialog()
        }
        binding.btAssignLot.setOnClickListener { moveToAssignLot() }
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun moveToAssignLot() {
        callBack?.navigateToAssignLot(vm.salesOrder,materialList)
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data?.deliveryDetails
                val postedLot = arrayListOf<VegaCoffeeSalesLots>()
                data?.forEach {
                    val lot = VegaCoffeeSalesLots()
                    lot.batchNumber = it.batchNumber.toString()
                    lot.delivery = it.delivery.toString()
                    lot.deliveryItem = it.deliveryItem.toString()
                    lot.salesTempId = salesOrder.salesTempId
                    lot.weighBridgeId = it.weighBridgeId.toString()
                    lot.deliveryFlag = it.deliveryFlag
                    lot.pickingFlag = it.pickingFlag
                    lot.pgiFlag = it.pgiFlag
                    lot.storageLossFlag = it.storageLossFlag
                    lot.endLotFlag = it.endLotFlag
                    postedLot.add(lot)
                }
                vm.updateLotWeightInfo(postedLot)
                postedLot.forEach { vm.deleteTempData(it.salesTempId) }
                if (response.data?.success!!) {
                    moveToSuccessPage(data)
                } else {
                    response.data?.message?.let { showErrorDialogWithFAQLink(requireContext(), it) }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.nigeria_custom_edit_text)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vm.salesOrder.remarks.isNullOrEmpty()) editValue.setText(vm.salesOrder.remarks)
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vm.salesOrder.remarks = editValue.text.toString()
                        binding.tvRemark.text = editValue.text.toString()
                        vm.saveWeighBridgeAndLotDetails()
                    } else {
                        editValue.error = getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<materialList>) {
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<materialList>,
            R.layout.item_nigeria_sales_material_list,
            ItemNigeriaSalesMaterialListBinding::inflate,
            { it, pos, bindItem ->
                materialUOM = it.meins.toString()
                bindItem.tvMaterialNew.text = it.materialDesc
                bindItem.tvSoWeight.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ").plus(it.meins)
                when (it.meins) {
                    "KG" -> {
                        bindItem.tvSoWeight.text =
                            it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(it.meins)
                    }
                    "MT" -> {
                        bindItem.tvSoWeight.text = (it.openQuantity.toString()).toDouble()
                            .formatThreeDigits()
                            .plus(" ").plus("MT")
                    }
                }

            })
    }

    private fun updateSalesItem(it: VegaCoffeeSalesOrderWithLots) {
        if (it.lineItems.size > 0) {
            if(it.lineItems.size == 1){
                binding.btAssignLot.visibility = View.GONE
                mergedBatchNumbers = listOf(it.lineItems[0].salesLot.batchNumber)

            }
            else
                binding.btAssignLot.visibility = View.VISIBLE

            var data = mutableListOf<VegaCoffeeSalesLotWithbags>()
            if (salesOrder.salesTempId.isNotEmpty()) data =
                it.lineItems.filter { it1 -> it1.salesLot.salesTempId.equals(salesOrder.salesTempId) } as MutableList<VegaCoffeeSalesLotWithbags>
            else {
                if (it.salesOrder.salesType.equals(SALES_TYPE_ANTICIPATED))
                    data =
                        it.lineItems.filter { it2 -> it2.salesLot.salesType.equals(SALES_TYPE_ANTICIPATED) } as MutableList<VegaCoffeeSalesLotWithbags>
                else
                    data = mutableListOf()
            }
            setUpAdapter(data)
        } else setUpAdapter(mutableListOf())
    }

    private fun setUpAdapter(lineItems: MutableList<VegaCoffeeSalesLotWithbags>) {
        vm.lots = lineItems.map { it.salesLot } as ArrayList<VegaCoffeeSalesLots>
        val isFailure = vm.lots.any { it.deliveryFlag!! || it.pickingFlag!! || it.pgiFlag!! }
        if (isFailure) binding.btProceed.text = getString(R.string.retry) else binding.btProceed.text =
            getString(R.string.confirm_dispatch)
        bagList.clear()
        lineItems.forEach {
            bagList.addAll(it.lineItems)
        }
        binding.rvLots.setUpAdapter(
            lineItems,
            R.layout.item_lot_nigeria_sales_summary,
            ItemLotNigeriaSalesSummaryBinding::inflate,
            { it, pos, bindItem ->
                val lot = it.salesLot
                bindItem.tvLotId.text = lot.batchNumber
                bindItem.tvStLocation.text = lot.storageLocationCode
                bindItem.tvGradeValue.text = lot.materialName
                bindItem.tvWeightValue.text = lot.weight.plus(" ").plus(lot.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                when (lot.unitOfMeasure) {
                    "KG" -> {
                        bindItem.tvWeightValue.text = lot.weight.plus(" ").plus(lot.unitOfMeasure)
                        bindItem.tvWeightToProcessValue.text =
                            lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                    }
                    "MT" -> {
                        bindItem.tvWeightValue.text = (lot.weight.toString()).plus(" ").plus("MT")
                        bindItem.tvWeightToProcessValue.text =
                            convertMtToKg(lot.editedWeight.toString()).plus(" ").plus("MT")
                    }
                }
                bindItem.ivEdit.setOnClickListener { view -> showLotEditDialog(it.salesLot, view) }
            }, itemClick = {

            })
    }

    private fun showLotEditDialog(lot: VegaCoffeeSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    callBack?.editLotDetails(salesOrder, lot)
                },
                { dismiss() })
        }
    }
    private fun validateProceed() {
        when {
            mergedBatchNumbers.isNullOrEmpty() -> showSnack("Assign Lot before Confirm Dispatch")
            else -> showConformationDialog()
        }
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        vm.postDeliveryDetail(
            VegaNigeriaSalesPostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                operatorName = "",
                batchNumber = "",
                delFlag = "",
                deliveryDetails = prepareDeliveryList(),
                weighmentType = salesOrder.salesType
            )
        )
    }

    private fun prepareDeliveryList(): List<VegaNigeriaSalesDeliveryDetail> {
        val list = ArrayList<VegaNigeriaSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in vm.lots) {
            val deliveryDetail = VegaNigeriaSalesDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.remarks = salesOrder.remarks
           /* deliveryDetail.netWeight = convertWeight(
                item.editedWeight?.trim() ?: "0.0",
                materialUOM, item.unitOfMeasure ?: ""
            )*/
           /* deliveryDetail.netWeight =
                item.editedWeight?.trim() ?: "0.0"*/
            deliveryDetail.createdDate = salesOrder.createdDate
            deliveryDetail.salesOrderNum = salesOrder.saleOrderId
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            val salesItem = materialList.filter { it.salesOrderId.equals(salesOrder.saleOrderId) }
                .filter { it.materialNumber.equals(item.materialCode) }
            if (salesItem.size > 0) deliveryDetail.salesItem = salesItem[0].salesItemNum
            deliveryDetail.weighBridgeId = item.weighBridgeId
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.pgiFlag = item.pgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.endLotFlag = item.endLotFlag
            deliveryDetail.unitsOfMeasure = materialUOM
            if(vm.lots.size == 1){
                deliveryDetail.recStorageLocationCode = item.storageLocationCode
                deliveryDetail.mergedBatchNumber = item.batchNumber
            }else{
                deliveryDetail.recStorageLocationCode = item.receivingStorageLocation
                deliveryDetail.mergedBatchNumber = item.mergedLotId
            }
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var startTime = ""
            var endTime = ""
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(
                    item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!
                )
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(
                    item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!
                )
                item1.noOfPallet =
                    if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if (item.salesType.equals(SALES_TYPE_ANTICIPATED)) deliveryDetail.grossWeight =
                deliveryDetail.netWeight
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            if ((vm.salesOrder.startTime).isNullOrEmpty()) {
                deliveryDetail.startTime = DateUtils.getCurrentTimeInMills().toString()
            } else {
                deliveryDetail.startTime = salesOrder.startTime
            }
            deliveryDetail.endTime = salesOrder.endTime
            deliveryDetail.turnAroundTime = salesOrder.turnAroundTime
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeSalesBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.formatThreeDigits().toString().trim()
                deliveryDetail.huno2 = "MT"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
                deliveryDetail.netWeight = ((deliveryDetail.grossWeight!!.toDouble()) - (bagItems[0].noOfPallet?.toDouble()!!) - (bagTareWeight)).formatThreeDigits().toString().trim()
            } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
            list.add(deliveryDetail)
        }
        return list
    }

    private fun convertWeight(weight: String, soUom: String, lotUom: String): String {
        if (soUom == lotUom) {
            return weight
        } else {
            if (soUom == UNIT_MT && lotUom == UNIT_KG) {
                return convertKgToMT(weight)
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return (weight)
        }
        return "0"
    }

    private fun moveToSuccessPage(data: List<VegaNigeriaSalesDeliveryDetail>?) {
        val deveryIds = arrayListOf<String>()
        val batchIds = arrayListOf<String>()
        var customMessage = ""

        val docNo = data?.filter { !it.documentNum.isNullOrEmpty() }?.map { it.documentNum.toString() } ?: listOf()
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        data?.forEach {
//            it.msgList?.toList()?.let { it1 -> deveryIds.addAll(it1) }
            batchIds.add(it.mergedBatchNumber.toString())
            deveryIds.add(it.delivery.toString())
        }
        customMessage = "Delivery Number ".plus(deveryIds.distinct().toString().replace("[", "").replace("]", "")).plus(" created successfully for merged batch number : ").plus(batchIds.distinct().toString().replace("[", "").replace("]", ""))

        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_dispatch))
        if (salesOrder.salesType == SALES_TYPE_ANTICIPATED) {
            intent.putExtra(AppUtils.PRINT_ENABLE, true)
            intent.putExtra("fromcoffee", true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, vm.lots)
        }
        if (!documentNo.isEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                deveryIds.toString().replace("[", "").replace("]", "").replace(
                    ",",
                    "\n"
                ).plus(getString(R.string.ws_success_docu_no, documentNo))
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, customMessage)

//        intent.putExtra(
//                AppUtils.SUB_TITLE,
//                deveryIds.toString().replace("[", "").replace("]", "").replace(",", "\n")
//            )
        startActivity(intent)
        requireActivity().finish()
    }

    fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaNigeriaLocalSalesAssignLot>,
        storageLocation: String
    ) {
        mergedBatchNumbers = mergedLotIds
        selectedStorageLocation = storageLocation
        assignLotList = list
        binding.tvAssignLot.text = "LOT IDs: ".plus(mergedLotIds.toString().replace("[", "").replace("]", ""))
        list.forEach {mergedLot ->
            vm.salesOrder.lotList.forEach {
                if( it.materialName == mergedLot.materialName)
                {
                    it.mergedLotId = mergedLot.mergedLotId
                    it.receivingStorageLocation = storageLocation // Added for Storage Location field
                }
            }
            vm.lots.forEach {
                    if( it.materialName == mergedLot.materialName)
                    {
                        it.mergedLotId = mergedLot.mergedLotId
                        it.receivingStorageLocation = storageLocation // Added for Storage Location field
                    }
            }
        }
        vm.saveWeighBridgeAndLotDetails()
    }
}
