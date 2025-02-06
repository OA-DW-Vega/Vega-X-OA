package com.olam.warehouse.vegax.salescoffee.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
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
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.data.domain.model.VegaCoffeeSalesDeliveryDetail
import com.olam.warehouse.vegax.salescoffee.data.domain.model.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.salescoffee.data.domain.model.materialList
import com.olam.warehouse.vegax.salescoffee.databinding.CustomEditTextBinding
import com.olam.warehouse.vegax.salescoffee.databinding.FragmentCoffeeSalesSummaryBinding
import com.olam.warehouse.vegax.salescoffee.databinding.ItemCoffeeSalesMaterialListBinding
import com.olam.warehouse.vegax.salescoffee.databinding.ItemLotCoffeeSalesSummaryBinding
import com.olam.warehouse.vegax.salescoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 8/21/2020.
 */
class VegaCoffeeSalesSummaryFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_coffee_sales_summary
    private val vm: VegaCoffeeSalesViewModel by viewModel()
    private lateinit var binding: FragmentCoffeeSalesSummaryBinding
    private var materialList = ArrayList<materialList>()
    private var bagList = arrayListOf<VegaCoffeeSalesBagMaterial>()
    private var salesOrder = VegaCoffeeSalesOrder()
    private var callBack: CallBack? = null
    private var materialUOM: String = ""
    private var bitmapValue = HashMap<Int, Bitmap?>()
    private var preQualityList = mutableListOf<VegaQualityParams>()

    interface CallBack {
        fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(
            model: VegaCoffeeSalesOrder,
            materialList: ArrayList<materialList>
        ) = VegaCoffeeSalesSummaryFragment().putArgs {
            putParcelable(SALES_ITEM, model)
            putParcelableArrayList(MATERIAL_LIST, materialList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCoffeeSalesSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        // vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/salescoffee/ui/VegaCoffeeSalesSelectSaleTypeFragment").title("IVC/Coffee/Sales/Sales Summary")
            .with(tracker)
    }
    private fun initExtra() {
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()
        salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALES_ITEM) ?: VegaCoffeeSalesOrder()
        vm.salesOrder = salesOrder
        if (vm.salesOrder.salesType == SALES_TYPE_WEIGHBRIDGE) {
            binding.tvTruckIDNo.visibility = View.VISIBLE
            binding.tvTruckValue.visibility = View.VISIBLE
            binding.tvTruckValue.text = salesOrder.truckNo
        }
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
    }

    private fun initUI() {
        vm.dispatchSalesItem.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateSalesItem(it) else updateSalesItem(VegaCoffeeSalesOrderWithLots()) })
        vm.getDispatchSalesItem(salesOrder.saleOrderId, salesOrder.salesType, salesOrder.salesTempId)
        binding.ivEditRemark.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>) {
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
            val bindingDia = CustomEditTextBinding.inflate(layoutInflater)
            customView(R.layout.custom_edit_text)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vm.salesOrder.remarks.isNullOrEmpty()) editValue.setText(vm.salesOrder.remarks)
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vm.salesOrder.remarks = editValue.text.toString()
                        binding.tvRemark.text = editValue.text.toString()
                        vm.saveWeighBridgeAndLotDetails()
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<materialList>) {
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<materialList>,
            R.layout.item_coffee_sales_material_list,
            ItemCoffeeSalesMaterialListBinding::inflate,
            { it, pos, bindItem ->
                materialUOM = it.meins.toString()
                bindItem.tvMaterial.text = it.materialDesc
                bindItem.tvSoWeight.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ").plus(it.meins)
            })
    }

    private fun updateSalesItem(it: VegaCoffeeSalesOrderWithLots) {
        if (it.lineItems.size > 0) {
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
        if (vm.salesOrder.salesType.equals(SALES_TYPE_WEIGHBRIDGE) && vm.lots.isNotEmpty() && vm.lots[0].endLotFlag == true)
            vm.getPreSamplingQualitydata(vm.lots[0].batchNumber, vm.lots[0].materialCode)
        val isFailure = vm.lots.any { it.deliveryFlag!! || it.pickingFlag!! || it.pgiFlag!! }
        if (isFailure) binding.btProceed.text = getString(R.string.retry) else binding.btProceed.text =
            getString(R.string.confirm_dispatch)
        bagList.clear()
        lineItems.forEach {
            bagList.addAll(it.lineItems)
        }
        binding.rvLots.setUpAdapter(
            lineItems,
            R.layout.item_lot_coffee_sales_summary,
            ItemLotCoffeeSalesSummaryBinding::inflate,
            { it, pos, bindItem ->
                val lot = it.salesLot
                bindItem.tvLotId.text = lot.batchNumber
                bindItem.tvStLocation.text = lot.storageLocationCode
                bindItem.tvGradeValue.text = lot.materialName
                bindItem.tvWeightValue.text = lot.weight.plus(" ").plus(lot.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    lot.editedWeight.plus(" ").plus(lot.unitOfMeasure)
                bindItem.ivEdit.setOnClickListener { view -> showLotEditDialog(it.salesLot, view) }
            },
            itemClick = {

            })
    }

    private fun showLotEditDialog(lot: VegaCoffeeSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //activity?.onBackPressed()
                    callBack?.editLotDetails(salesOrder, lot)
                },
                { dismiss() })

        }
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (vm.salesOrder.salesType != SALES_TYPE_WEIGHBRIDGE)
                        postDelivery()
                    else postWBDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        vm.postDeliveryDetail(
            VegaCoffeeSalesPostRequest(
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

    private fun postWBDelivery() {
        vm.postDeliveryDetail(
            VegaCoffeeSalesPostRequest(
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

    private fun prepareDeliveryList(): List<VegaCoffeeSalesDeliveryDetail> {
        val list = ArrayList<VegaCoffeeSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in vm.lots) {
            val deliveryDetail = VegaCoffeeSalesDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.remarks = salesOrder.remarks
            if (vm.salesOrder.salesType.equals(SALES_TYPE_WEIGHBRIDGE) && item.endLotFlag == true)
                deliveryDetail.qualityDetails = preQualityList
            deliveryDetail.netWeight = convertWeight(
                item.editedWeight?.trim() ?: "0.0",
                materialUOM, item.unitOfMeasure ?: ""
            )
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
            if (vm.salesOrder.salesType.equals(SALES_TYPE_WEIGHBRIDGE)) {
                deliveryDetail.weighBridgeId = vm.salesOrder.salesTempId
                deliveryDetail.truckDirection = "IN"
                deliveryDetail.pgiFlag = true
            } else {
                deliveryDetail.pgiFlag = item.pgiFlag
            }

            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.endLotFlag = item.endLotFlag
            deliveryDetail.unitsOfMeasure = materialUOM
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
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if (item.salesType.equals(SALES_TYPE_ANTICIPATED)) deliveryDetail.grossWeight = deliveryDetail.netWeight
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            deliveryDetail.startTime = salesOrder.startTime
            deliveryDetail.endTime = salesOrder.endTime
            deliveryDetail.turnAroundTime = salesOrder.turnAroundTime
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeSalesBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.formatThreeDigits().toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
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
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight)
        }
        return "0"
    }

    private fun moveToSuccessPage(data: List<VegaCoffeeSalesDeliveryDetail>?) {
        val deveryIds = arrayListOf<String>()
        val docNo = data?.filter { !it.documentNum.isNullOrEmpty() }?.map { it.documentNum.toString() } ?: listOf()
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        data?.forEach {
            it.msgList?.toList()?.let { it1 -> deveryIds.addAll(it1) }
        }
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
            intent.putExtra(
                AppUtils.SUB_TITLE,
                deveryIds.toString().replace("[", "").replace("]", "").replace(",", "\n")
            )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                       true -> {
                           preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    //showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }
}
