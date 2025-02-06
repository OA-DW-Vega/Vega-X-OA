package com.olam.warehouse.vegax.mtntghana.ui.weighscale

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.data.domain.model.TextNavListValues
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntPurchaseOrders
import com.olam.warehouse.vegax.mtntghana.databinding.FragmentGhanaMtntWsSummaryBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaLotSummaryBinding
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaMaterialLayoutBinding
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntLotRemoveListener
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaMtntViewModel
import com.olam.warehouse.vegax.mtntghana.ui.VegaGhanaReplaceFragmentCallback
import com.olam.warehouse.vegax.mtntghana.utils.JSON_TEXT_UPDATE
import com.olam.warehouse.vegax.mtntghana.utils.WEIGHSCALE
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class VegaGhanaMtntWeighScaleSummaryFragment : BaseFragment(), VegaGhanaMtntLotRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaGhanaCocoaDispatchLots>()
    override val layoutResourceId: Int = R.layout.fragment_ghana_mtnt_ws_summary
    private lateinit var binding: FragmentGhanaMtntWsSummaryBinding
    private var callBack: VegaGhanaReplaceFragmentCallback? = null
    private var summaryObj: VegaCocoaDispatchWB? = null
    private val vm: VegaGhanaMtntViewModel by viewModel()
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private lateinit var vegaCoffeeDeliveryPost: VegaGhanaMtntDeliveryPost
    private var vegaCocoaMtntWithLots: VegaGhanaCocoaMtntWithLots? = null
    var isPostCreated: Boolean = false
    private var purchaseOrder = ArrayList<VegaGhanaMtntPurchaseOrders>()
    private var workFlowData: WorkflowFields? = null
    private var jsonData = mutableListOf<String>()
    private var textUpdate = ArrayList<String>()

    companion object {
        fun newInstance(data: VegaCocoaDispatchWB) = VegaGhanaMtntWeighScaleSummaryFragment().putArgs {
            putParcelable("summaryData", data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaMtntWsSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.weighScaleDeliveryPost.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        return binding.root
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable("summaryData")
    }

    private fun initUi() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "27")
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
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            updateProcessType(it)
        })
        vm.getProcessTypeList(getCurrentKey())
        binding.tvRemark.gone()
        binding.cvRemark.gone()

        binding.header.tvTruckNoValue.text = summaryObj?.vehicleNumber
        binding.tvRemarkValue.text = summaryObj?.remarks
        binding.header.tvDestValue.text =
                summaryObj?.recPlantId.plus("-").plus(summaryObj?.plantName).plus("-").plus(summaryObj?.destinationWH).plus("-").plus(summaryObj?.destinationWHName)
        binding.header.tvDateValue.text = summaryObj?.erdat
        binding.header.tvStoNumberValue.text =
                summaryObj?.purchaseDocNum.plus("-").plus(summaryObj?.purchaseDocDesc)
        binding.header.tvContactValue.text = summaryObj?.driverPhoneNumber
        binding.header.tvDriverNameValue.text = summaryObj?.driverName
        binding.header.tvPhyDocNoValue.text = summaryObj?.frbnr1
        binding.btProceed.setOnClickListener { showConformationDialog() }
        binding.ivEdit.setOnClickListener { showUpdateRemarkDialog() }
        binding.tvRemarkValue.text = summaryObj?.remarks
        vm.purchaseOrderOffline.observe(viewLifecycleOwner, Observer { updatePurchaseOrderOffline(it) })

        vm.getPurchaseOrderOffline()
    }
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
    private fun textUpdteList(): List<TextNavListValues> {
        val list = ArrayList<TextNavListValues>()
        textUpdate.forEach {
            if(it.toString().isNotEmpty()) {
                val textNavListValues = TextNavListValues()
                var textValue = (it.toString()).split("-")
                if (textValue[1].trim().equals("Z003")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue =
                            binding.header.tvDriverNameValue.text.toString().plus("/")
                                .plus(binding.header.tvContactValue.text.toString())
                } else if (textValue[1].trim().equals("Z006")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.header.tvTruckNoValue.text.toString()
                } else if (textValue[1].trim().equals("ZS01")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = textValue[0].trim()
                } else if (textValue[1].trim().equals("Z011")) {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = binding.header.tvPhyDocNoValue.text.toString()
                } else {
                    textNavListValues.textId = textValue[1].trim()
                    textNavListValues.textValue = textValue[0].trim()
                }
                list.add(textNavListValues)
            }
        }
        return list
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
                vegaGhanaMtntPurchaseOrders.storageLocationCode = it.storageLocationCode
                vegaGhanaMtntPurchaseOrders.storageLocationName = it.storageLocationName
                vegaGhanaMtntPurchaseOrders.issueLocation = it.issueLocation

                purchaseOrder.add(vegaGhanaMtntPurchaseOrders)
            }

        }
    }

    private fun updateLocalDbData(weighBridge: VegaGhanaCocoaMtntWithLots) {
        dispatchLotsList.clear()
        setUpAdapter(weighBridge.lineItems)
        dispatchLotsList.addAll(weighBridge.lineItems)
        calculateAndUpdateWeightToDispatch()
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })

        vm.getBagItems()

    }

    private fun setUpMaterialAdapter() {
        binding.header.rvMaterialList.setUpAdapter(
            vm.materialModelList,
            R.layout.item_ghana_material_layout,
            ItemGhanaMaterialLayoutBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.materialName
                bindItem.tvStoWeightValue.text =
                    it.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(it.uom)
                bindItem.tvDispatchWeightValue.text = it.dispatchWeight?.plus(" MT")
            })
    }

    private fun setUpAdapter(list: List<VegaGhanaCocoaDispatchLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_ghana_lot_summary,
            ItemGhanaLotSummaryBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocation.text = item.storageLocationCode
                val weight = item.weight?.toDouble()
                val enteredWeight = item.editedWeight?.toDouble()
                val totalLoss = weight?.minus(enteredWeight!!)
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                bindItem.tvEditedWeight.text =
                    item.editedWeight?.toDouble()?.formatThreeDigits().toString().plus(" ")
                        .plus(item.unitOfMeasure)
                if (item.isEndLot == true) {
                    bindItem.tvTotalWeightLoss.text =
                        totalLoss?.formatThreeDigits().plus(" ").plus("MT")
                } else
                    bindItem.tvTotalWeightLoss.text = "NA"
                bindItem.tvUnit.visibility = View.GONE
                bindItem.tvEditedWeight.visibility = View.VISIBLE
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_ghana_edit_gray))
                bindItem.cbSelectAll.visibility = View.GONE
                bindItem.tvSelectAll.visibility = View.GONE
                bindItem.cbEndLot.visibility = View.GONE
//            tvEndLot.visibility = View.GON000000000jiol;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                bindItem.etWeight.visibility = View.GONE
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun prepareOfflinePostDelivery() {
        var summedWeight = 0.000
        var sum = 0.000
        dispatchLotsList.forEach { item ->
            var updatedWeight = item.weight.toString().toDouble() - (item.editedWeight.toString().toDouble())
            vm.updateStockDetails(updatedWeight.toString(), item.batchNumber)
/*            item.weight = updatedWeight.toString()
            vm.saveLot(item)*/
            summedWeight = sum + item.editedWeight.toString().toDouble()
        }
        vm.dispatchWh.isOfflineData = true
        vm.saveWeighBridgeDetails()
        var po = purchaseOrder
            .filter { it.purchaseDocNum == vm.dispatchWh.purchaseDocNum.toString() }
            .filter { it.purchaseDocDesc == vm.dispatchWh.purchaseDocDesc.toString() }
        var updatedOpenQuantity = po[0].openQuantity.toString().toDouble() - summedWeight
        vm.updateMtntPurchaseOrderDetails(updatedOpenQuantity.toString(), po[0].purchaseDocNum.toString())
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
        vegaCoffeeDeliveryPost = VegaGhanaMtntDeliveryPost(
            getCurrentKey(),
            getPlantDetails(),
            "",
            prepareDeliveryList(),
            textUpdteList(),
            WEIGHSCALE,
            binding.header.tvContactValue.text.toString(),
            binding.header.tvTruckNoValue.text.toString(),
            binding.header.tvDriverNameValue.text.toString(),
            binding.header.tvStoNumberValue.text.toString(),
            notificationFlag = workFlowData?.NotificationFlag?.trim().equals("1"),
            nextWorkFlowRole = workFlowData?.workflowModule,
            navId = workFlowData?.workflowId,
            currentWorkFlowRole = workFlowData?.module
        )
        vm.postWeighScaleDeliveryDetails(vegaCoffeeDeliveryPost)
    }


    private fun prepareDeliveryList(): List<VegaGhanaMtntDeliveryDetail> {
        val list = ArrayList<VegaGhanaMtntDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaGhanaMtntDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = summaryObj?.plantId
            deliveryDetail.recPlantId = summaryObj?.recPlantId
            deliveryDetail.fromVendorCode = summaryObj?.fromVendorCode
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.vehicleNumber = summaryObj?.vehicleNumber
            deliveryDetail.endLotFlag = item.isEndLot ?: false
            deliveryDetail.createdDate = summaryObj?.startTime
            vm.materialModelList.forEach {
                if (item.materialCode == it.materialCode) {
                    deliveryDetail.purchaseDocNum = it.purchaseOrderNum
                    deliveryDetail.purchaseDocDesc = it.purchaseOrderDesc
                    deliveryDetail.soWeight = it.soWeight
                }
            }
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.startTime = summaryObj?.startTime
            deliveryDetail.endTime = summaryObj?.endTime
            deliveryDetail.turnAroundTime = summaryObj?.turnAroundTime ?: "0"
            deliveryDetail.weighBridgeId = item.weighScaleWbId ?: ""
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.isPgiFlag = item.isPgiFlag
            deliveryDetail.storageLossFlag = item.storageLossFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.year = year.toString()
            deliveryDetail.frbnr1 = summaryObj?.frbnr1
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
                deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            list.add(deliveryDetail)
        }
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

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (AppUtils.isOnline()) postDelivery()
                    else prepareOfflinePostDelivery()
//                postDelivery()
                },
                { dismiss() })
        }
    }

    private fun showConformationBackNav() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_back)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
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
                message = message.plus("WeighScaleId: ".plus("" + it.weighBridgeId).plus(" Batch Number: ").plus(it.batchNumber).plus(" DeliveryNumber: ").plus(it.delivery).plus("\n"))
//                message =
//                    "WeighScaleId".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
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
        var bag = bagItems.filter { it.weighBridgeId == dispatchLotsList[0].weighBridgeId }
        bag.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun showUpdateRemarkDialog() {
        showDialog(
            getString(R.string.update_remark_title),
            object : DialogClick {
                override fun onPositive(remark: String) {
                    summaryObj?.remarks = remark
                    binding.tvRemarkValue.text = remark
                    vm.updateRemarks(remark, true, summaryObj?.weighBridgeId ?: "")
                }
            },
            true, summaryObj?.remarks ?: ""
        )
    }

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
