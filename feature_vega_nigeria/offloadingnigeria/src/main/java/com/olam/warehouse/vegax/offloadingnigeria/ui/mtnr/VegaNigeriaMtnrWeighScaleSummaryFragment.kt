package com.olam.warehouse.vegax.offloadingnigeria.ui.mtnr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.offloadingnigeria.R
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingnigeria.databinding.FragmentNigeriaMtnrWsSummaryBinding
import com.olam.warehouse.vegax.offloadingnigeria.databinding.ItemNigeriaMtnrWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingnigeria.ui.VegaNigeriaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList


class VegaNigeriaMtnrWeighScaleSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_nigeria_mtnr_ws_summary
    private lateinit var binding: FragmentNigeriaMtnrWsSummaryBinding
    private var callBack: VegaNigeriaOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private val vm: VegaNigeriaOffloadingViewModel by viewModel()
    private var vegaCoffeeReceivingData = VegaCoffeeReceiving()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var workFlowData: WorkflowFields? = null



    companion object {
        fun newInstance(data: VegaCoffeeReceiving) =
            VegaNigeriaMtnrWeighScaleSummaryFragment().putArgs {
            putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNigeriaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaMtnrWsSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "18")
        binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }
        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaMtntResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    response.data?.data?.let { moveToSuccessPage(it) }
                    vm.updateStatus(vegaCoffeeReceivingData.delivery.toString())
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }

    }

    private fun initExtra() {
        vegaCoffeeReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoffeeReceiving()
        binding.tvDestValue.text =
            vegaCoffeeReceivingData.supplierCode.plus(" - ")
                .plus(vegaCoffeeReceivingData.supplierName)
        binding.tvDriverNameValue.text =
            vegaCoffeeReceivingData.storageLocationCode.plus(" - ")
                .plus(vegaCoffeeReceivingData.storageLocationName)
        binding.tvStoNumberValue.text = vegaCoffeeReceivingData.delivery
        binding.tvTruckNoValue.text = vegaCoffeeReceivingData.vehicleNumber
        binding.tvRemarkValue.text = vegaCoffeeReceivingData.remarks

        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer {
            updateOBDDetails(it)
        })
        vm.getOBDDetails(vegaCoffeeReceivingData.delivery)
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        batchList.clear()
        if (data != null) {
            if (data.lineItems.size > 0) {
                data.lineItems.forEach {
                    /*var edWeight = 0.0
                    val bags = data.lineItems.filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()*/
                    batchList.add(it.lots)
                    bagList.addAll(it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }
                        .filter { it1 ->
                            it1.batchNumber.equals(
                                it.lots.batch
                            )
                        })
                }
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.offloading_custom_edit_text_nigeria)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vegaCoffeeReceivingData.remarks.isNullOrEmpty()) editValue.setText(
                vegaCoffeeReceivingData.remarks
            )
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vegaCoffeeReceivingData.remarks = editValue.text.toString()
                        binding.tvRemarkValue.text = editValue.text.toString()
                        vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(list: List<VegaCoffeeReceiveLots>) {
        val lots = list as MutableList
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_nigeria_mtnr_weighscale_lot_card_layout,
            ItemNigeriaMtnrWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batch
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.uom)
                val dispatchWeight = item.weight?.toDouble()
                bindItem.tvScaleGradeValue.text = item.materialName
                var receivingKg = item.editedWeight
                val editedWeight =
                    if (receivingKg.isNullOrEmpty()) "0.0" else receivingKg.toString().toDouble()
                        .formatThreeDigits()
                if(item.uom.equals(UNIT_EA,ignoreCase = true)){
                    bindItem.tvScaleDispatchValue.text = item.bagCount.toString()
                    bindItem.tvDispatchUOMValue.text = UNIT_EA
                } else{
                    bindItem.tvScaleDispatchValue.text = editedWeight
                    bindItem.tvDispatchUOMValue.text = "KG"
                    val receivingWeight = receivingKg?.toDouble()
                    val weightLoss = receivingWeight?.let { dispatchWeight?.minus(it) }
                    bindItem.tvTotalWeightLossValue.text =
                        weightLoss?.formatThreeDigits()?.plus(" ")?.plus("KG")
                }
                bindItem.ivEdit.visible()
                bindItem.tvAddWeight.visibility = View.GONE
                bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }
            })
    }

    private fun showLotEditDialog(lot: VegaCoffeeReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    callBack?.replaceFragment(EDIT_LOT, lot)
                },
                { dismiss() })
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


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun postDelivery() {
        var count = 0
        if ((PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0")).isNullOrEmpty()) {
            count = 0
        } else {
            count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
        }
        if (count != 0 && DateUtils.isFirstDaysOfMonth(requireContext(), count)) {
            UIUtils.showPostingDateDialog(requireContext(), count, object : UIUtils.DialogClick {
                override fun onPositive(date: String) {
                    var postingDate =
                        DateUtils.oneFormatToOtherFormat(date, "dd-MMM-yyyy", "yyyyMMdd")
                    vm.postOffloadingDetail(
                        VegaNigeriaOffloadingPostRequest(
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            operatorName = "",
                            batchNumber = "",
                            delFlag = "",
                            deliveryDetails = prepareDeliveryList(postingDate),
                            weighmentType = "",
                            vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
                            vehicleType = vegaCoffeeReceivingData.vehicleType,
                            contactNumber = vegaCoffeeReceivingData.contactNumber,
                            driverName = vegaCoffeeReceivingData.driverName,
                            notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                            nextWorkFlowRole = workFlowData?.workflowRole,
                            navId = workFlowData?.workflowId,
                            currentWorkFlowRole = workFlowData?.module,
                            environment = BuildConfig.BUILD_TYPE
                        )
                    )
                }
            })
        } else {
            vm.postOffloadingDetail(
                VegaNigeriaOffloadingPostRequest(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    operatorName = "",
                    batchNumber = "",
                    delFlag = "",
                    deliveryDetails = prepareDeliveryList(""),
                    weighmentType = "",
                    vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
                    vehicleType = vegaCoffeeReceivingData.vehicleType,
                    contactNumber = vegaCoffeeReceivingData.contactNumber,
                    driverName = vegaCoffeeReceivingData.driverName,
                    notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                    nextWorkFlowRole = workFlowData?.workflowRole,
                    navId = workFlowData?.workflowId,
                    currentWorkFlowRole = workFlowData?.module,
                    environment = BuildConfig.BUILD_TYPE
                )
            )
        }

    }

    private fun prepareDeliveryList(postingDate: String): List<VegaNigeriaOffloadingDeliveryDetail> {
        var currentKey = getCurrentKey()
        val list = ArrayList<VegaNigeriaOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaNigeriaOffloadingDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batch }.distinct()
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.remarks = vegaCoffeeReceivingData.remarks
            deliveryDetail.recStorageLocationCode = vegaCoffeeReceivingData.storageLocationCode
            deliveryDetail.storageLocationCode = vegaCoffeeReceivingData.supplierCode
            deliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
//            deliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
            deliveryDetail.purchaseDocDesc = item.ebelp
            deliveryDetail.plantId = vegaCoffeeReceivingData.materialCode
            deliveryDetail.recPlantId = vegaCoffeeReceivingData.materialCode
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.postingDate = postingDate
            if ((item.uom.toString()).equals(UNIT_MT)) {
                deliveryDetail.unitsOfMeasure = UNIT_MT
                deliveryDetail.netWeight = item.editedWeight
            } else if ((item.uom.toString()).equals(UNIT_KG)) {
                deliveryDetail.unitsOfMeasure = item.uom
                deliveryDetail.netWeight =
                    convertMtToKg(item.editedWeight, item.uom)?.trim() ?: "0.0"
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
                        ?.div(item1.noOfPallet?.toInt()?:0) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(if(item1.tareWeight?.isNotEmpty() == true)item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())?:0.0 else 0.0).plus(palletAvg?:0.0)
                bagTareWeight = bagTareWeight.plus(if(item1.tareWeight?.isNotEmpty() == true)item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())?:0.0 else 0.0)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            if ((item.uom.toString()).equals(UNIT_MT)) {
                deliveryDetail.grossWeight = grossWeight.toString()
            } else if ((item.uom.toString()).equals(UNIT_KG)) {
                deliveryDetail.grossWeight = convertMtToKg(grossWeight.toString(), item.uom)?.trim()
            } else if ((item.uom.toString()).equals(UNIT_EA)) {
                val juteBagCount = bagItems.filter { it1 -> it1.bagType.equals(JUTE_BAG, ignoreCase = true) }.sumOf { it.bagCount.toInt() }
                deliveryDetail.netWeight = juteBagCount.toString()
                deliveryDetail.grossWeight = juteBagCount.toString()
                deliveryDetail.unitsOfMeasure = UNIT_EA
            }
            deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                bagItems.forEach {
                    if(it.tareWeight.isNullOrEmpty())it.tareWeight="0"
                    if(it.tareWeight1.isNullOrEmpty())it.tareWeight1="0"
                    if ((item.uom.toString()).equals(UNIT_MT)) {
                        it.grossWeight = it.grossWeight
                        it.netWeight = it.netWeight
                        var totalTareWeight =
                            it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                        it.tareWeight = totalTareWeight.toString()
                    } else if ((item.uom.toString()).equals(UNIT_KG)) {
                        it.grossWeight = convertMtToKg(it.grossWeight, item.uom)!!
                        it.netWeight = convertMtToKg(it.netWeight, item.uom)!!
                        var totalTareWeight =
                            it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                        it.tareWeight = convertMtToKg(totalTareWeight.toString(), item.uom)!!
                    }

                    if (currentKey.split("_")[2].contains("SESA")) {
                        it.unitsOfMeasure = UNIT_MT
                    } else if (currentKey.split("_")[2].contains("CASH")) {
                        //  it.tareWeight = totalTareWeight?.formatNDigits(6)
                        it.unitsOfMeasure = UNIT_KG
                    }
                    it.palletWeight = it.palletWeight
                    it.palletAverage = it.palletAverage
                }
                if ((item.uom.toString()).equals(UNIT_MT)) {
                    deliveryDetail.huno = UNIT_MT
                    deliveryDetail.huwt =
                        bagTareWeight.formatThreeDigits().trim()
                    deliveryDetail.huno2 = UNIT_MT
                } else if ((item.uom.toString()).equals(UNIT_KG)) {
                    deliveryDetail.huno = UNIT_KG
                    deliveryDetail.huwt =
                        convertKgToMT(bagTareWeight.toString(), item.uom)?.toDouble()
                            ?.formatThreeDigits()?.trim()
                    deliveryDetail.huno2 = UNIT_KG
                }


                if (currentKey.split("_")[2].contains("SESA")) {
                    deliveryDetail.huno = "KG"
                    deliveryDetail.huno2 = "KG"
                } else if (currentKey.split("_")[2].contains("CASH")) {
                    deliveryDetail.huno = item.uom
                    deliveryDetail.huno2 = item.uom
                }
                if ((item.uom.toString()).equals(UNIT_EA)) {
                    val juteBagItem = bagItems.filter { it1 -> it1.bagType.equals(JUTE_BAG, ignoreCase = true) }
                    val newJuteBagItem = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
                    juteBagItem.forEach {
                        val newBagItem = it.copy()
                        newBagItem.grossWeight = it.bagCount
                        newBagItem.netWeight = it.bagCount
                        newBagItem.tareWeight = "0"
                        newBagItem.tareWeight1 = "0"
                        newJuteBagItem.add(newBagItem)
                    }
                    deliveryDetail.huwt2 = ""
                    deliveryDetail.bagList = newJuteBagItem
                }else {
                    deliveryDetail.huwt2 = bagItems[0].palletAverage
                    deliveryDetail.bagList = bagItems
                }
                deliveryDetail.nohu1 =
                    bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet

            } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
            list.add(deliveryDetail)
        }
        if(list.indexOf(list.find { it.unitsOfMeasure.equals(UNIT_EA) })==0)
            return list.asReversed()
        return list
    }

    /*private fun convertWeight(weight: String, soUom: String, lotUom: String): String? {
        if (soUom == lotUom) {
            return weight
        } else {
            if (soUom == UNIT_MT && lotUom == UNIT_KG) {
                return convertKgToMT(weight, lotUom)
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight, lotUom)
        }
        return "0"
    }*/

    private fun moveToSuccessPage(data: VegaMtntResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.wb_id_is, data.wbId))
      /*  intent.putExtra(
            AppUtils.PRINT_ENABLE,
            true
        )
        val tallyKeys = ArrayList<String>()
        tallyKeys.add(data.encodedImageContent ?: "")
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)*/
        startActivity(intent)
        requireActivity().finish()
    }
}
