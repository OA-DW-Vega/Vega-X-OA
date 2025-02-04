package com.olam.warehouse.vegax.mtntghanacocoa.ui.weighbridge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.TextNavListValues
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaDispatchMtntSummaryBinding
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghanacocoa.utils.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaCocoaCocoaMtntSummaryFragment : BaseFragment(),
    VegaGhanaCocoaMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_ghana_cocoa_dispatch_mtnt_summary
    private lateinit var binding: FragmentGhanaCocoaDispatchMtntSummaryBinding
    private var callBack: VegaGhanaCocoaReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaGhanaCocoaMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaGhanaMtntDeliveryPost
    private var vegaCocoaMtntWithLots: VegaGhanaCocoaMtntWithLots? = null
    private var jsonData = mutableListOf<String>()
    private var textUpdate = ArrayList<String>()
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaGhanaCocoaCocoaMtntSummaryFragment()
            .putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoaDispatchMtntSummaryBinding.inflate(inflater)
        initExtra()
        initUi()

        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()
        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {

        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, Observer {
            if (it != null) updateLocalDbData(it)
        })

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })
        vm.getProcessTypeList(getCurrentKey())

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
        })
        vm.getMaterials()

        binding.tvTruckIdValue.text = summaryObj?.truckID
        binding.tvTransportVendorValue.text = summaryObj?.fromVendorCode
        binding.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvDriverNameValue.text = summaryObj?.driverName
        binding.tvDriverPhoneValue.text = summaryObj?.driverPhoneNumber
        binding.tvSDWaybillNoValue.text = summaryObj?.sidingDepotwayBillNumber
        binding.tvEvacuationNoValue.text = summaryObj?.evacuationCertificateNumber
        binding.tvDriverLicenseNumberValue.text = summaryObj?.driverLicenseNumber
        binding.tvRecLocationValue.text =
            getMTNTPlantName(summaryObj?.destinationWH!!, true).plus(" - ")
                .plus(summaryObj?.destinationWHName)
        //summaryObj?.destinationWH.plus(" - ").plus(summaryObj?.destinationWHName)
        binding.tvDispatchLocationValue.text =
            summaryObj?.storageLocationCode.plus(" - ").plus(summaryObj?.storageLocationName)
        binding.tvProductValue.text =
            summaryObj?.materialCode.plus("-").plus(summaryObj?.materialName)
        binding.tvNoofbagsalue.text = summaryObj?.purchaseQuantity
        binding.tvBillofLadingValue.text = summaryObj?.frbnr1
        binding.tvDeparturePointValue.text = summaryObj?.departurePoint
        binding.tvRouteValue.text = summaryObj?.route
        binding.btProceed.setOnClickListener { showConformationDialog() }

        /*binding.tvTruckValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.clSummary.tvStoNo.text = getString(R.string.obd_number)
        val times = summaryObj?.erdat?.split('(', ')')
        binding.clSummary.tvDateValue.text = times?.get(1).let { it1 ->
            it1?.let { it2 ->
                DateUtils.getUTCDateTime(
                    it2,
                    App.getAppContext()
                )
            }
        }
        binding.clSummary.tvStoNoValue.text = summaryObj?.delivery
        binding.clSummary.tvMaterialName.text = summaryObj?.materialName
        binding.clSummary.tvStoWeightValue.text = summaryObj?.deliveryQty.plus(" ").plus(summaryObj?.deliveryUOM)
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        binding.tvRemarkValue.text = summaryObj?.remarks*/


    }

    private fun updateLocalDbData(weighBridge: VegaGhanaCocoaMtntWithLots) {
        vegaCocoaMtntWithLots = weighBridge
        setUpMaterialAdapter()
        dispatchLotsList.clear()
        //setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })

        vm.getBagItems()
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        var bag = bagItems.filter { it.weighBridgeId == dispatchLotsList[0].weighBridgeId }
        bag.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }


    private fun setUpMaterialAdapter() {
        val list = ArrayList<VegaGhanaPurchaseOrderMaterialModel>()
        list.add(
            VegaGhanaPurchaseOrderMaterialModel(
                vm.dispatchWh.weighBridgeId,
                vm.dispatchWh.materialCode ?: "",
                vm.dispatchWh.materialName,
                vm.dispatchWh.netWeight,
                vm.dispatchWh.unitsOfMeasure
            )
        )
    }

   /* private fun setUpAdapter(list: List<VegaGhanaCocoaDispatchLots>) {
        *//* val lots = list as MutableList
         binding.rvLots.setUp(lots, R.layout.item_ghana_cocoa_lot_summary, { item, pos ->
             tvLotId.text = item.batchNumber
             tvStLocation.text = item.storageLocationCode
             tvWeightValue.text =
                 item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
             tvGradeValue.text = item.materialName
             tvEditedWeight.text =
                 item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ").plus(item.unitOfMeasure)
             tvUnit.visibility = View.GONE
             tvEditedWeight.visibility = View.VISIBLE
             ivClose.setImageDrawable(ivClose.context.getDrawable(R.drawable.ic_ghana_edit_gray))
             cbSelectAll.visibility = View.GONE
             tvSelectAll.visibility = View.GONE
             cbEndLot.visibility = View.GONE
             tvEndLot.visibility = View.GONE
             etWeight.visibility = View.GONE
             ivClose.setOnClickListener { itemRemoved(item) }
         })*//*
    }*/

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_TEXT_UPDATE)) {
                var receiveLocation = (JSONObject(it).getJSONArray(JSON_TEXT_UPDATE).get(0)).toString().split(",")
                receiveLocation.forEach {  it1 ->
                    textUpdate.add(((it1.split(":")[0]).replace("{","").replace("\"", "")).plus(" - ").plus((it1.split(":")[1]).replace("}","").replace("\"", "")))
                }
            }

        }
    }

    private fun postDelivery() {
        dispatchLotsList.forEach {
            var dfh = it.batchNumber
        }
        vegaCoffeeDeliveryPost = VegaGhanaMtntDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            if (dispatchLotsList.size > 0 == true) dispatchLotsList.get(0).batchNumber else "",
            prepareDeliveryList(),
            textUpdteList(),
            WEIGHSCALE,
            summaryObj?.vehicleNumber,
            summaryObj?.driverLicenseNumber,
            "",
            summaryObj?.driverPhoneNumber.toString(),
            summaryObj?.driverName.toString()
        )
        //vm.postDeliveryDetail(vegaCoffeeDeliveryPost)
        vm.postWeighScaleDeliveryDetails(
            vegaCoffeeDeliveryPost,
            binding.tvSDWaybillNoValue.text.toString()
        )
    }

    private fun textUpdteList(): List<TextNavListValues> {
        val list = ArrayList<TextNavListValues>()
        textUpdate.forEach {
            if(it.toString().isNotEmpty()) {
                val textNavListValues = TextNavListValues()
                var textValue = (it.toString()).split("-")
                if (textValue[1].trim().equals("Z003")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvDriverNameValue.text.toString()
                } else if (textValue[1].trim().equals("Z006")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvTruckNoValue.text.toString()
                } else if (textValue[1].trim().equals("ZS01")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvEvacuationNoValue.text.toString()
                } else if (textValue[1].trim().equals("Z011")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvSDWaybillNoValue.text.toString()
                } else {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = textValue[0].trim()
                }
                list.add(textNavListValues)
            }
        }
        return list
    }


    private fun prepareDeliveryList(): List<VegaGhanaMtntDeliveryDetail> {
        val list = ArrayList<VegaGhanaMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        var dispatch = vegaCocoaMtntWithLots?.dispatch        //for (item in dispatchLotsList) {
        var bagItems = bagList.filter { it.batchNumber == dispatch?.batchNumber && it.baseMaterial == dispatch.materialCode }
        dispatchLotsList.forEach {
            val deliveryDetail = VegaGhanaMtntDeliveryDetail()
            deliveryDetail.batchNumber = it.batchNumber
            deliveryDetail.materialCode = dispatch?.materialCode
            deliveryDetail.materialName = dispatch?.materialName
            deliveryDetail.plantId = dispatch?.plantId
            deliveryDetail.recPlantId = dispatch?.recPlantId
            val departurePointCodeValue = summaryObj?.departurePoint.toString().split("-")
            deliveryDetail.departurePoint = departurePointCodeValue[0]
            // deliveryDetail.endLotFlag = dispatch?.isEndLot ?: false
            //  deliveryDetail.netWeight = dispatch?.editedWeight
            deliveryDetail.createdDate = summaryObj?.startTime
            deliveryDetail.frbnr1 = summaryObj?.frbnr1
            //deliveryDetail.driverLicenseNumber = summaryObj?.driverLicenseNumber
            deliveryDetail.purchaseDocNum = summaryObj?.purchaseDocNum
            deliveryDetail.purchaseDocDesc = summaryObj?.purchaseDocDesc
            //deliveryDetail.recStorageLocationCode = dispatch?.recStorageLocationCode
            deliveryDetail.storageLocationCode = dispatch?.storageLocationCode
            /*deliveryDetail.recStorageLocationCode = dispatch?.storageLocationCode
            deliveryDetail.storageLocationCode = dispatch?.recStorageLocationCode*/
            //deliveryDetail.weighBridgeId = summaryObj?.weighBridgeId
            deliveryDetail.netWeight = summaryObj?.purchaseQuantity
            deliveryDetail.grossWeight = summaryObj?.purchaseQuantity
            deliveryDetail.unitsOfMeasure = BAG
            deliveryDetail.huno2 = BAG
            deliveryDetail.startTime = summaryObj?.startTime
            deliveryDetail.endTime = summaryObj?.endTime
            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.pickingFlag = summaryObj?.pickingFlag ?: false
            deliveryDetail.storageLossFlag = dispatch?.storageLossFlag
            deliveryDetail.deliveryFlag = summaryObj?.deliveryFlag ?: false
            deliveryDetail.deliveryItem = summaryObj?.deliveryItem
            deliveryDetail.delivery = summaryObj?.delivery
            deliveryDetail.year = year.toString()
            val routeCodeValue = summaryObj?.routeLocCode.toString().split("-")
            deliveryDetail.routeLocCode = routeCodeValue[0]
            val transportVendorCodeValue = summaryObj?.fromVendorCode.toString().split("-")
            deliveryDetail.fromVendorCode = transportVendorCodeValue[0]
            deliveryDetail.textId = summaryObj?.textId
            deliveryDetail.textValue = summaryObj?.textValue
        deliveryDetail.startTime =(DateUtils.getCurrentTimeInMills()).toString()
        deliveryDetail.endTime = (DateUtils.getCurrentTimeInMills()).toString()
        deliveryDetail.purchaseQuantity = summaryObj?.purchaseQuantity
        //Bag and pallet details
        var grossWeight = 0.0
        var tareWeight = 0.0
        var bagTareWeight = 0.0
        /*var startTime = ""
        var endTime = ""*/
        bagItems.forEach { item1 ->
            val palletAvg =
                if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                    ?.div(item1.noOfPallet?.toInt()!!) else 0.0
            grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                .plus(palletAvg!!)
            bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
            /*if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
            if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()*/
            item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
        }
        if (bagItems.isNotEmpty()) {
            val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
            val dat = bagItems.sortedByDescending { it.createdPosition }
            bagSort.addAll(dat)
            bagItems = bagSort.asReversed()
            deliveryDetail.huno = bagItems[0].unitsOfMeasure
            deliveryDetail.huwt = bagTareWeight.toString().trim()
            deliveryDetail.huno2 = "MT"
            deliveryDetail.huwt2 = bagItems[0].palletAverage
            deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
            deliveryDetail.nohu2 = bagItems[0].noOfPallet
            if(grossWeight.toString().trim().isNullOrEmpty()){
                deliveryDetail.grossWeight ="0"
            }else {
                deliveryDetail.grossWeight =grossWeight.toString().trim()
            }
            deliveryDetail.bagList = bagItems
        } else {
            var bag = VegaCocoaSweepingBagMaterial()
            // bag.bagMaterialCode = dispatch?.bagMaterialCode.toString()
            // bag.bagType = dispatch?.bagType.toString()
            bag.bagCount = dispatch?.purchaseQuantity.toString()

            if (bagTypeList.size == 1) {
                bag.bagMaterialCode = bagTypeList[0].bagMaterialCode.toString().trim()
                bag.tareWeight = bagTypeList[0].tareWeight.toString().trim()
                bag.bagType = bagTypeList[0].bagType.toString().trim()
            }
            var totalweight: Double =
                (calculateWeight(dispatch?.materialCode.toString())).toDouble()
            bag.grossWeight = (totalweight * (summaryObj?.purchaseQuantity!!.toDouble())).toString()
            bag.netWeight = (totalweight * (summaryObj?.purchaseQuantity!!.toDouble())).toString()
            /*if(dispatch?.grossWeight.toString().trim().isNullOrEmpty()){
                bag.grossWeight ="0"
            }else {
                bag.grossWeight = dispatch?.grossWeight.toString()
            }
            if(dispatch?.netWeight.toString().isNullOrEmpty()){
                bag.netWeight ="0"
            }else {
                bag.netWeight = dispatch?.netWeight.toString()
            }*/
            bag.noOfPallet = "0"
            bag.palletAverage = "0"
            bag.palletWeight = "0"
            /*if(dispatch?.tareWeight.toString().isNullOrEmpty()){
                bag.tareWeight ="0"
            }else {
                bag.tareWeight = dispatch?.tareWeight.toString()
            }*/
            bag.unitsOfMeasure = "KG"
            val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
            bagSort.add(bag)
            bagItems = bagSort
            deliveryDetail.bagList = bagItems
            // deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
            // deliveryDetail.grossWeight = grossWeight.toString().trim()
        }
        list.add(deliveryDetail)
         }
        return list
    }


    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_mtnt)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>) {
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
                    vm.updateGhanaSyncStatus(vegaCocoaMtntWithLots!!)
                } else {
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaGhanaMtntDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message = message.plus("WeighScaleId: ".plus("" + it.weighBridgeId).plus(" Batch Number: ").plus(it.batchNumber).plus(" DeliveryNumber: ").plus(it.delivery).plus("\n"))
//                message =
//                    "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun moveToSuccessPage(deliveryId: String?,docNo: List<String>) {
        dispatchLotsList.forEach {
            vm.deleteBag(it.batchNumber)
        }
        val documentNo = if (docNo.size > 0) docNo.toString().replace("[", "").replace("]", "") else ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch)
        ) else intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch_offline)
        )
        /*intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId))*/
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
    }

    /*private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    summaryObj?.remarks = remark
                    // binding.tvRemarkValue.text = remark
                    vm.updateRemarks(remark, true, summaryObj?.weighBridgeId ?: "")
                }
            },
            true, summaryObj?.remarks ?: ""
        )
    }*/

    override fun itemRemoved(item: VegaGhanaCocoaDispatchLots) {
        activity?.onBackPressed()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    private fun calculateWeight(materialCode: String): String {
        var netWeight = ""
        var uom =
            ((uomDetails.filter { (MATERIAL_CODE.plus(it.materialCode)).equals(materialCode) }).filter {
                it.fromUom.equals(BAG)
            }).single()
        netWeight =
            (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)).toString()
        return netWeight
    }
}
