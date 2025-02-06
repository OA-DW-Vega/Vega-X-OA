package com.olam.warehouse.vegax.mtntghanacocoa.ui.offline

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
import com.google.gson.Gson
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.TextNavListValues
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.FragmentGhanaCocoaMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntghanacocoa.ui.VegaGhanaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghanacocoa.utils.BAG
import com.olam.warehouse.vegax.mtntghanacocoa.utils.JSON_TEXT_UPDATE
import com.olam.warehouse.vegax.mtntghanacocoa.utils.WEIGHSCALE
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaMtntOfflineSummaryFragment : BaseFragment(), VegaGhanaCocoaMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_ghana_cocoa_mtnt_ws_summary
    private lateinit var binding: FragmentGhanaCocoaMtntWsSummaryBinding
    private var callBack: VegaGhanaCocoaReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaGhanaCocoaMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaGhanaMtntDeliveryPost
    private var vegaCocoaMtntWithLots: VegaGhanaCocoaMtntWithLots? = null
    var isPostCreated: Boolean = false
    private var offlineStock = ArrayList<VegaEcuadorDispatchStocks>()
    private var jsonData = mutableListOf<String>()
    private var textUpdate = ArrayList<String>()
    private var supplierList = mutableListOf<VegaVendor>()
    private var mtntPlantName: String? = ""
    private var mtntPlantList = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaGhanaMtntOfflineSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoaMtntWsSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
       // binding.ivEdit.visibility = View.GONE
        vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
        vm.weighBridgeWithLotsSource.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                vm.dispatchWh = it.dispatch
                vm.lotList.clear()
                vm.lotList.addAll(it.lineItems)
                vm.materialModelList.clear()
                vm.materialModelList.addAll(it.materialList)
                vegaCocoaMtntWithLots = it
                updateLocalDbData(it)
            }
        })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        vm.getSuppliers()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })
        vm.getProcessTypeList(getCurrentKey())

//        binding.header.tvTruckNoValue.text = summaryObj?.vehicleNumber
//        //binding.tvRemarkValue.text = summaryObj?.remarks
//        binding.header.tvDestValue.text =
//            summaryObj?.recPlantId.plus("-").plus(summaryObj?.plantName).plus("-").plus(summaryObj?.destinationWH).plus("-").plus(summaryObj?.destinationWHName)
//        binding.header.tvDateValue.text = summaryObj?.erdat
//        binding.header.tvStoNumberValue.text =
//            summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
//        binding.header.tvContactValue.text = summaryObj?.driverPhoneNumber
//        binding.header.tvDriverNameValue.text = summaryObj?.driverName

        binding.tvTruckIdValue.text = summaryObj?.truckID
        binding.tvTransportVendorValue.text = summaryObj?.fromVendorCode
        binding.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvDriverNameValue.text = summaryObj?.driverName
        binding.tvDriverPhoneValue.text = summaryObj?.driverPhoneNumber
        binding.tvDriverLicenseNumberValue.text = summaryObj?.driverLicenseNumber
        binding.tvRecLocationValue.text =
                summaryObj?.destinationWH.plus(" - ")
                        .plus(summaryObj?.destinationWHName)
        mtntPlantName = summaryObj?.destinationWH
        //summaryObj?.destinationWH.plus(" - ").plus(summaryObj?.destinationWHName)
        binding.tvDispatchLocationValue.text =
            summaryObj?.storageLocationCode.plus(" - ").plus(summaryObj?.storageLocationName)
        binding.tvProductValue.text =
            summaryObj?.materialCode.plus("-").plus(summaryObj?.materialName)
        binding.tvNoofbagsalue.text = summaryObj?.purchaseQuantity
        binding.tvBillofLadingValue.text = summaryObj?.frbnr1
        binding.tvDeparturePointValue.text = summaryObj?.departurePoint
        binding.tvRouteValue.text = summaryObj?.route
        binding.tvSDWaybillNoValue.text = summaryObj?.sidingDepotwayBillNumber
        binding.tvEvacuationNoValue.text = summaryObj?.evacuationCertificateNumber

        binding.btProceed.setOnClickListener {
//            showConformationDialog()
            activity?.onBackPressed()
        }
        binding.btProceed.text = "OK"
//        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        // binding.tvRemarkValue.text = summaryObj?.remarks
//        vm.stocksOffline.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateOfflineStock(it) })
//        vm.fetchStocksOffline()
    }

    /*private fun updateOfflineStock(offlineStocks: List<VegaEcuadorDispatchStocks>?) {
        offlineStock = offlineStocks as ArrayList<VegaEcuadorDispatchStocks>
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
    private fun updateLocalDbData(weighBridge: VegaGhanaCocoaMtntWithLots) {
        dispatchLotsList.clear()
        //setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })
        vm.getBagItems()


    }

    private fun setUpMaterialAdapter() {
//        binding.header.rvMaterialList.setUp(vm.materialModelList, R.layout.item_ghana_cocoa_material_layout, { it, pos ->
//            tvMaterialName.text = it.materialName
//            tvStoWeightValue.text = it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
//                .plus(it.uom)
//            tvDispatchWeightValue.text = it.dispatchWeight?.plus(" MT")
//        })
    }

    /*private fun setUpAdapter(list: List<VegaGhanaCocoaDispatchLots>) {
//        val lots = list as MutableList
//        binding.rvLots.setUp(lots, R.layout.item_ghana_cocoa_lot_summary, { item, pos ->
//            tvLotId.text = item.batchNumber
//            tvStLocation.text = item.storageLocationCode
//            var stock = offlineStock.filter { it.batchNumber == item.batchNumber }
//            val weight = item.weight?.toDouble()
//            val stockWeight = stock[0].weight?.toDouble()
//            val enteredWeight = item.editedWeight?.toDouble()
//            val totalLoss = weight?.minus(enteredWeight!!)
//            tvWeightValue.text =
//                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
////            tvWeightValue.text =
////                stockWeight?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
//            tvGradeValue.text = item.materialName
//            tvEditedWeight.text =
//                item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ").plus(item.unitOfMeasure)
//            if (item.isEndLot == true) {
//                tvTotalWeightLoss.text = totalLoss?.formatThreeDigits().plus(" ").plus("MT")
//            } else
//                tvTotalWeightLoss.text = "NA"
//            tvUnit.visibility = View.GONE
//            tvEditedWeight.visibility = View.VISIBLE
//            ivClose.visibility = View.GONE
////            ivClose.setImageDrawable(ivClose.context.getDrawable(R.drawable.ic_ghana_edit_gray))
//            cbSelectAll.visibility = View.GONE
//            tvSelectAll.visibility = View.GONE
//            cbEndLot.visibility = View.GONE
////            tvEndLot.visibility = View.GONE
//            etWeight.visibility = View.GONE
////            ivClose.setOnClickListener { itemRemoved(item) }
//        })
    }*/

    private fun prepareOfflinePostDelivery() {
        dispatchLotsList.forEach {
            var updatedWeight = it.weight.toString().toDouble() - (it.editedWeight.toString().toDouble())
            vm.updateStockDetails(updatedWeight.toString(), it.batchNumber)
        }
        moveToOfflineSuccessPage()
    }

    private fun moveToOfflineSuccessPage() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.msg_mtnt_offline_success))
        intent.putExtra(AppUtils.SUB_TITLE, "Temp ID :".plus(dispatchLotsList[0].weighBridgeId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun postDelivery() {
        if(supplierList.size>0) {
            vegaCoffeeDeliveryPost = VegaGhanaMtntDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                "",
                prepareDeliveryList(),
                textUpdteList(),
                WEIGHSCALE,
                summaryObj?.vehicleNumber,
                summaryObj?.driverLicenseNumber,
                "",
                summaryObj?.driverPhoneNumber.toString(),
                summaryObj?.driverName.toString(),
                supplierList.get(0).purchaseOrgType
            )
            vm.postWeighScaleDeliveryDetails(
                vegaCoffeeDeliveryPost,
                binding.tvSDWaybillNoValue.text.toString()
            )
        }
    }

    private fun textUpdteList(): List<TextNavListValues> {
        val list = ArrayList<TextNavListValues>()
        textUpdate.forEach {
            if(it.toString().isNotEmpty()) {
                val textNavListValues = TextNavListValues()
                var textValue = (it.toString()).split("-")
                if (textValue[1].trim().equals("Z003")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue =
                        binding.tvDriverLicenseNumberValue.text.toString().plus("/")
                            .plus(binding.tvDriverNameValue.text.toString()).plus("/")
                            .plus(binding.tvDriverPhoneValue.text.toString())
                } else if (textValue[1].trim().equals("Z006")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.tvTruckNoValue.text.toString()
                } else if (textValue[1].trim().equals("ZS01")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = summaryObj?.evacuationCertificateNumber
                } else if (textValue[1].trim().equals("Z011")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = summaryObj?.sidingDepotwayBillNumber
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
        var dispatch = vegaCocoaMtntWithLots?.dispatch
        val deliveryDetail = VegaGhanaMtntDeliveryDetail()
        var bagItems = bagList.filter { it.batchNumber == dispatch?.batchNumber && it.baseMaterial == dispatch.materialCode }
        deliveryDetail.batchNumber = dispatch?.batchNumber
        deliveryDetail.materialCode = dispatch?.materialCode
        deliveryDetail.materialName = dispatch?.materialName
        var recievingLocation = mtntPlantList.single {
            it.plantId.startsWith("3") && it.plantName.equals(mtntPlantName)
        }
        deliveryDetail.recPlantId = recievingLocation.plantId
//            deliveryDetail.plantId = summaryObj?.recPlantId
        // deliveryDetail.netWeight = dispatch?.editedWeight
        //deliveryDetail.vehicleNumber = summaryObj?.vehicleNumber
        // deliveryDetail.endLotFlag = dispatch?.isEndLot ?: false
        deliveryDetail.createdDate = summaryObj?.startTime
        vm.materialModelList.forEach {
            if (dispatch?.materialCode == it.materialCode) {
                deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
            }
        }
        deliveryDetail.recStorageLocationCode = dispatch?.storageLocationCode
        deliveryDetail.storageLocationCode = dispatch?.storageLocationCode
        // deliveryDetail.unitsOfMeasure = dispatch?.unitOfMeasure
        deliveryDetail.startTime = summaryObj?.startTime
        deliveryDetail.frbnr1 = summaryObj?.frbnr1
        deliveryDetail.endTime = summaryObj?.endTime
        deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
        // deliveryDetail.weighBridgeId = dispatch?.weighScaleWbId ?: ""
        deliveryDetail.deliveryFlag = dispatch?.deliveryFlag ?: false
        deliveryDetail.pickingFlag = dispatch?.pickingFlag ?: false
        //deliveryDetail.isPgiFlag = dispatch?.isPgiFlag
        deliveryDetail.storageLossFlag = dispatch?.storageLossFlag
        deliveryDetail.delivery = dispatch?.delivery
        deliveryDetail.deliveryItem = dispatch?.deliveryItem
        deliveryDetail.year = year.toString()
        deliveryDetail.routeLocCode = summaryObj?.routeLocCode
        deliveryDetail.huno2 = BAG
        deliveryDetail.unitsOfMeasure = BAG
        deliveryDetail.textId = summaryObj?.textId
        deliveryDetail.textValue = summaryObj?.textValue
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
            deliveryDetail.grossWeight = grossWeight.toString().trim()
            deliveryDetail.bagList = bagItems
        } else {
            //deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
            //  deliveryDetail.grossWeight = grossWeight.toString().trim()
        }
        list.add(deliveryDetail)
        return list
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        dispatchLotsList.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
        }
        vm.materialModelList.forEach { it.dispatchWeight = materialWeightMap[it.materialCode] }
        setUpMaterialAdapter()
    }

    /*private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (AppUtils.isOnline()) postDelivery()
                    else prepareOfflinePostDelivery()
//                postDelivery()
                },
                { dismiss() })
        }
    }*/

    private fun showConformationBackNav() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_back)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.updateSyncStatus(vegaCocoaMtntWithLots!!)
                    activity?.finish()
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
                isPostCreated = true
                if (response.data?.success == true) {
                    var docNo = listOf<String>()
                    docNo =
                        response.data?.data?.filter { !it.documentNum.isNullOrEmpty() }
                            ?.map { it.documentNum.toString() }
                            ?: listOf()
                    moveToSuccessPage(prepareSuccessMessage(response.data?.data), docNo)
                    vm.updateGhanaSyncStatus(vegaCocoaMtntWithLots!!)
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
                            isPgiFlag = item.isPgiFlag
                            storageLossFlag = item.storageLossFlag
                        }
                    }
                    vm.lotList.clear()
                    vm.dispatchWh = summaryObj!!
                    vm.lotList.addAll(dispatchLotsList)
                    vm.saveWeighBridgeAndLotDetails()
                    vm.getWeighBridgeWithLotAndMaterial(summaryObj?.weighBridgeId ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun prepareSuccessMessage(data: List<VegaGhanaMtntDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message =
                    "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun moveToSuccessPage(deliveryId: String?, docNo: List<String>) {
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

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

   /* private fun showUpdateRemarkDialog() {
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

    fun backNav() {
        showConformationBackNav()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
