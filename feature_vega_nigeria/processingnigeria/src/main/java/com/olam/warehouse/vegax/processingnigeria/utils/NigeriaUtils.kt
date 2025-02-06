package com.olam.warehouse.vegax.processingnigeria.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.NigeriaProcessingFgrnLotDetails
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingFgrnPost
import com.olam.warehouse.vegax.processingnigeria.data.domain.model.VegaNigeriaProcessingRMINLotDetails
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

const val RMIN = "rmin"
const val FGRN = "fgrn"
const val ADDLOT = "AddLot"
const val SUBSTAGES = "SubStages"
const val EDITLOT = "EditLot"
const val Lot_List = "LotList"
const val MODEL_BUNDLE = "model"
const val SHIFT = "Shift"
const val SUMMARY = "Summary"
const val FROM_SUMMARY = "fromSummary"
const val FRAG_ITEM = "frag_item"
const val IS_PO = "is_po"
const val FRAG_ID = "frag_id"
const val FRAG_GRADES = "frag_grades"
const val RMIN_GRADES = "rmin_grades"
const val FRAG_PENDING = "frag_pending"
const val FRAG_ADD_WEIGHT = "frag_add_weight"
const val FRAG_ADD_WEIGHT_EDIT = "frag_add_weight_edit"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val UPDATE_WEIGHT = "UpdateWeight"
const val BATCH_LIST = "batch_list"
const val POBOM = "PoBom"

//const val JSON_PROCESSING_TYPE_LIST = "PROCESSING_TYPE_LIST"
const val JSON_PROCESSING_TYPE_LIST_EN = "PROCESSING_TYPE_LIST_EN"
const val JSON_PROCESSING_TYPE_LIST_FR = "PROCESSING_TYPE_LIST_FR"

const val JSON_SHIFT_DETAILS_LIST = "SHIFT_DETAILS_LIST"
const val JSON_SHIFT_DETAILS_LIST_EN = "SHIFT_DETAILS_LIST_EN"
const val JSON_CUSTOMER_LIST_DETAILS = "CUSTOMER"
const val JSON_SHIFT_DETAILS_LIST_FR = "SHIFT_DETAILS_LIST_FR"
const val LANGUAGE = "languageCode"
const val MULTIPLE_LOT = "addMultiple"
const val FRAG_CREATE_LOT = "frag_create_lot"
const val FRAG_SHIFT = "frag_shift"
const val FRAG_BAG_CONSUMP = "frag_bag"
const val FRAG_SUMMARY = "frag_summary"
const val FRAG_FILTER = "frag_filter"
const val FRAG_SUMMARY_BACK = "frag_summary_back"
const val MATERIAL_CODE = "material_code"
const val MATERIAL_NAME = "material_name"
const val IS_EDIT = "is_edit"
const val FULL_FILTER = "full_filter"
const val FILTER_WH_LOC = "filter_wh_loc"
const val FILTER_START_RANGE = "filter_start_range"
const val FILTER_END_RANGE = "filter_end_range"
const val FILTER_ABOVE_RANGE = "filter_above_range"
const val STORAGE_LOC = "storage_loc"
const val LOT_ID = "lot_id"
const val CREATE_NEW_LOT = "create_new_lot"
const val STOCK_LIST = "stock_list"
const val JSON_PROCESS_TYPE_LIST = "PROCESS_TYPE_LIST"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}

fun weightToProcess(
    rminList: List<VegaProcessingList>?,
    rfgrnList: List<VegaProcessingList>?,
    uom: String?
): Double {
    var rminWeight = 0.0
    var fgrnWeight = 0.0
    rminList?.forEach { rmin ->
        if (!rmin.netWeight.isNullOrEmpty()) {
            when (rmin.unitsOfMeasure) {
                "KG" -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
               // "MT" -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble().times(1000))
                "MT" -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
                else -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
            }
        }
    }

    rfgrnList?.forEach { fgrn ->
        if (!fgrn.netWeight.isNullOrEmpty()) {
            when (fgrn.unitsOfMeasure) {
                "KG" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())
               // "MT" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble().times(1000))
                "MT" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())
                else -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())

            }
        }
    }
    return when (uom) {
        "KG" -> rminWeight - fgrnWeight
//        "MT" -> (rminWeight - fgrnWeight).div(1000)
        else -> rminWeight - fgrnWeight
    }
    //return rminWeight - fgrnWeight
    /*rminWeight = when {
        !rminList.isNullOrEmpty() -> {
            when {
                rminList[0].netWeight.toString().isEmpty() -> 0.0
                else -> rminList.sumByDouble { it.netWeight!!.toDouble() }
            }
        }
        else -> 0.0
    }
    fgrnWeight = when {
        !rfgrnList.isNullOrEmpty() -> {
            when {
                rfgrnList[0].netWeight.toString().isEmpty() -> 0.0
                else -> rfgrnList.sumByDouble { it.netWeight!!.toDouble() }
            }
        }
        else -> 0.0
    }*/
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String?): String? {
    return try {
        weight?.toDouble()?.div(1000)?.formatThreeDigits()
    } catch (e:NumberFormatException){"0.0"}
}

fun processToProcessingMaterial(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeFgrnGradesMatrialWeights {
    val data = VegaCoffeeFgrnGradesMatrialWeights()
    data.id = bagMaterial.id
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight
    data.bagType = bagMaterial.bagType
    data.bagCount = bagMaterial.bagCount
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.tareWeight = bagMaterial.tareWeight
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.palletAverage = bagMaterial.palletAverage
    data.batchNumber = bagMaterial.batchNumber
    return data
}

fun processingMaterialToProcess(bagMaterial: VegaCoffeeFgrnGradesMatrialWeights): VegaCocoaSweepingBagMaterial {
    val data = VegaCocoaSweepingBagMaterial()
    data.id = bagMaterial.id
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight.toString()
    data.bagType = bagMaterial.bagType
    data.bagCount = bagMaterial.bagCount
    data.tareWeight = bagMaterial.tareWeight
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.palletAverage = bagMaterial.palletAverage
    return data
}

fun prepareRminPostRequest(
    poOrderDetails: VegaCoffeeRminProcessing?,
    lotDetails: VegaCoffeeRminItemWithGrades,
    workFlowData : WorkflowFields,

): VegaNigeriaProcessingFgrnPost {

    val poReq = VegaNigeriaProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remark
    poReq.shiftType = poOrderDetails?.shift
    poReq.rmin = true
    poReq.notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0")
    poReq.nextWorkFlowRole = workFlowData?.workflowRole
    poReq.navId = workFlowData?.workflowId
    poReq.currentWorkFlowRole = workFlowData?.module
    poReq.environment = BuildConfig.BUILD_TYPE
    val processList = mutableListOf<NigeriaProcessingFgrnLotDetails>()

    lotDetails.gradeWithLotsAndBags?.forEach {
        val grade = it.rminGrades
        it.lotItems?.forEach {
            val processLot = NigeriaProcessingFgrnLotDetails()
            val batch = it.rminLots.batchNumber
            processLot.batchNumber = it.rminLots.batchNumber
            processLot.materialCode = it.rminLots.materialCode
            processLot.plant = it.rminLots.plantId
            processLot.endLotFlag = it.rminLots.isEndLot ?: false
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var netWeight = 0.0
            val bagSort = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
            val filterList =
                it.lotBagItems?.filter { it.batchNumber == batch }?.sortedByDescending { it.createdTime }
            if (filterList != null) {
                filterList.forEach {
                    it.unitsOfMeasure = "KG"
                }
                bagSort.addAll(filterList)
            }
            it.lotBagItems = bagSort.asReversed()
            filterList?.forEach{
                when (it.unitsOfMeasure) {
                    "KG" -> {
                        it.grossWeight = (it.grossWeight)
                        it.netWeight = (it.netWeight)!!
                        it.tareWeight = (it.tareWeight)!!
                        it.unitsOfMeasure = "KG"
                        it.palletWeight = (it.palletWeight)!!
                        it.palletAverage = (it.palletAverage)!!
                    }
                    "MT" -> {
                        it.grossWeight = (it.grossWeight)
                        it.netWeight = (it.netWeight)!!
                        it.tareWeight = (it.tareWeight)!!
                        it.unitsOfMeasure = "MT"
                        it.palletWeight = (it.palletWeight)!!
                        it.palletAverage = (it.palletAverage)!!
                    }
                }
            }
            it.lotBagItems?.forEach { item ->
                val palletAvg =
                    if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                        ?.div(item.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
            }
            when (it.rminLots.meins) {
                "MT" -> netWeight = ((grossWeight.minus(tareWeight)).formatThreeDigits()).toDouble()
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
//            processLot.netWeight = convertKgToMT(netWeight.toString())
            processLot.bagCount = it.lotBagItems?.sumOf { it1 -> it1.bagCount.toInt() }.toString()
            if (it.lotBagItems!!.isNotEmpty())
            {
                processLot.bagType = it.lotBagItems!![0].bagType
                processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
                processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
                processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
                processLot.huno = it.lotBagItems!![0].unitsOfMeasure
                when (it.lotBagItems!![0].unitsOfMeasure) {
                    "KG" ->
                        processLot.huwt = (bagTareWeight.toString()).trim()
                    "MT" ->
                        processLot.huwt = (bagTareWeight.toString()).trim()
                }
                processLot.huno = "KG"
                processLot.huno2 = "KG"
                processLot.nohu1 =
                    it.lotBagItems?.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                processLot.nohu2 = it.lotBagItems!![0].noOfPallet
                processLot.storageLocationCode = it.rminLots.storageLocationCode
                processLot.unitsOfMeasure = grade.meins
                if (processLot.unitsOfMeasure.equals("MT")) {
                    //processLot.netWeight = (netWeight.toString())
                    processLot.netWeight = convertKgToMT(netWeight.toString())
                    //processLot.netWeightMT = convertKgToMT(processLot.netWeight)
                    processLot.menge = (netWeight.toString())
                    processLot.menge = grade.weight
                    processLot.grossWeight = convertKgToMT(grossWeight.toString())
                    //processLot.grossWeight = (grossWeight.toString().trim())
                    //processLot.grossWeightMT = convertKgToMT(processLot.grossWeight)
                    processLot.huwt2 = (it.lotBagItems!![0].palletAverage)
                    processLot.huwt = ((bagTareWeight.toString()).trim())
                } else {
                    //processLot.netWeight = netWeight.toString()
                    processLot.netWeight = convertKgToMT(netWeight.toString())
                    //processLot.netWeightMT = convertKgToMT(processLot.netWeight)
                    processLot.menge = netWeight.toString()
                    processLot.menge = grade.weight
                    processLot.grossWeight = convertKgToMT(grossWeight.toString())
                    //processLot.grossWeight = grossWeight.toString().trim()
                    //processLot.grossWeightMT = convertKgToMT(processLot.grossWeight)
                    processLot.huwt2 = it.lotBagItems!![0].palletAverage
                    processLot.huwt = (bagTareWeight.toString()).trim()
                }
            }else
            {
                processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
                processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
                processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
                processLot.huno = "KG"
                processLot.huno2 = "KG"
                processLot.nohu1 =
                    it.lotBagItems?.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                processLot.storageLocationCode = it.rminLots.storageLocationCode
                processLot.unitsOfMeasure = grade.meins
                if (processLot.unitsOfMeasure.equals("MT")) {
                    //processLot.netWeight = (netWeight.toString())
                    processLot.netWeight = convertKgToMT(netWeight.toString())
                    processLot.menge = (netWeight.toString())
                    processLot.menge = grade.weight
                    processLot.grossWeight = convertKgToMT(grossWeight.toString())
                    processLot.huwt = ((bagTareWeight.toString()).trim())
                } else {
                    //processLot.netWeight = netWeight.toString()
                    processLot.netWeight = convertKgToMT(netWeight.toString())
                    //processLot.netWeightMT = convertKgToMT(processLot.netWeight)
                    processLot.menge = netWeight.toString()
                    processLot.menge = grade.weight
                    processLot.grossWeight = convertKgToMT(grossWeight.toString())
                    processLot.huwt = (bagTareWeight.toString()).trim()
                }
            }
            processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.deliveryItem = grade.deliveryItem
            processLot.movementType = grade.bwart
            processLot.phase = grade.phase
            processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
            processLot.processOrderNum = grade.processOrderNo
            processLot.rsnum = grade.rsNum
            processLot.rspos = grade.rsPos
            processLot.xchpf = grade.xchpf
            processLot.storageLossFlag = it.rminLots.storageLossFlag
            //processLot.bagMaterialCode = bagItem.bagMaterialCode
            processLot.startTime = grade.startTime
            processLot.endTime = grade.endTime
            processLot.bagList = it.lotBagItems
            processList.add(processLot)
        }
    }
    poReq.processingLotDtls = processList
    return poReq
}


fun prepareFgrnPostRequest(
    poOrderDetails: VegaCoffeeFgrnItems?,
    lotDetails: List<VegaCoffeeFgrnGradesWithBagItems>?,sapMaterialList: ArrayList<VegaMaterial> = ArrayList<VegaMaterial>()
): VegaNigeriaProcessingFgrnPost {

    val poReq = VegaNigeriaProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.materialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remarks
    poReq.shiftType = poOrderDetails?.shiftSelection
    val processList = mutableListOf<NigeriaProcessingFgrnLotDetails>()
    var complaintMaterial= ArrayList<VegaMaterial>()
    var nonComplaintMaterial= ArrayList<VegaMaterial>()

    //sapMaterial list only applicable for T&T here
    if(sapMaterialList.isNotEmpty()) {
        complaintMaterial = sapMaterialList.filter { it.complainceFlag == Constants.COMPLAINT } as ArrayList<VegaMaterial>
        nonComplaintMaterial = sapMaterialList.filter { it.complainceFlag == Constants.NON_COMPLAINT } as ArrayList<VegaMaterial>
    }

    lotDetails?.forEach { it ->
        val processLot = NigeriaProcessingFgrnLotDetails()
        processLot.batchNumber = it.fgrnGrades.batchNumber
        processLot.complianceFlag=""

        //sapMaterial list only applicable for T&T here
        if(sapMaterialList.isNotEmpty()){
            val comList=complaintMaterial.any { it1-> it.fgrnGrades.materialCode.contains(it1.materialCode) }
            val nonComList=nonComplaintMaterial.any { it1-> it.fgrnGrades.materialCode.contains(it1.materialCode) }
            if(comList){
                processLot.complianceFlag=Constants.EUDR_QP_VALUE
            }else if(nonComList){
                processLot.complianceFlag=Constants.ATTR_UNKNOWN_QP_VALUE
            }
        }
        processLot.materialCode = it.fgrnGrades.materialCode
        processLot.plant = it.fgrnGrades.plantId

        var grossWeight = 0.0
        var tareWeight = 0.0
        var bagTareWeight = 0.0
        var netWeight = 0.0
        val bagSort = arrayListOf<VegaCoffeeFgrnGradesMatrialWeights>()
        val dat = it.bagItems?.sortedByDescending { it.createdTime }
        if (dat != null) {
            bagSort.addAll(dat)
        }
        it.bagItems = bagSort.asReversed()
        dat?.forEach {
            when (it.unitsOfMeasure) {
                "KG" -> {
                    it.grossWeight = (it.grossWeight)
                    it.netWeight = (it.netWeight)!!
                    it.tareWeight = it.tareWeight
//                   it.tareWeight = (it.tareWeight)!!
                    it.unitsOfMeasure = "KG"
                    it.palletWeight = (it.palletWeight)!!
                    it.palletAverage = (it.palletAverage)!!
                }
                "MT" -> {
                    it.grossWeight = (it.grossWeight)
                    it.netWeight = (it.netWeight)!!
                    it.tareWeight = it.tareWeight
//                   it.tareWeight = convertKgToMT(it.tareWeight)!!
                    it.unitsOfMeasure = "MT"
                    it.palletWeight = (it.palletWeight)!!
                    it.palletAverage = (it.palletAverage)!!
                }

            }

        }
        it.bagItems?.forEach { item ->
            val palletAvg =
                if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                    ?.div(item.noOfPallet?.toInt()!!) else 0.0
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg!!)
            bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
        }
        when (it.fgrnGrades.meins) {
            "MT" -> netWeight = ((grossWeight.minus(tareWeight)).formatThreeDigits()).toDouble()
            "KG" -> netWeight = grossWeight.minus(tareWeight)
            else -> netWeight = grossWeight.minus(tareWeight)
        }

        processLot.netWeight = convertKgToMT(netWeight.toString())
        //processLot.netWeight = netWeight.toString()
        //processLot.netWeightMT = convertKgToMT(processLot.netWeight)
        processLot.vendorCode = poOrderDetails?.vendor
        processLot.storageLocationCode = it.fgrnGrades.lotStorageLocationCode
        processLot.unitsOfMeasure = it.fgrnGrades.meins
        processLot.bagCount = it.bagItems?.sumOf { it1 -> it1.bagCount.toInt() }.toString()
        processLot.bagType = it.bagItems!![0].bagType
        processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.fgrnGrades.deliveryItem
        processLot.menge = it.fgrnGrades.weight
        processLot.movementType = it.fgrnGrades.bwart
        processLot.phase = it.fgrnGrades.phase
        processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
        processLot.processOrderNum = it.fgrnGrades.processOrderNo
        processLot.rsnum = it.fgrnGrades.rsNum
        processLot.rspos = it.fgrnGrades.rsPos
        processLot.xchpf = it.fgrnGrades.xchpf
        processLot.huno = it.bagItems!![0].unitsOfMeasure
        processLot.huwt = bagTareWeight.toString().trim()
        processLot.huno2 = "KG"
        processLot.huwt2 = it.bagItems!![0].palletAverage
        processLot.nohu1 = it.bagItems?.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
        processLot.nohu2 = it.bagItems!![0].noOfPallet
        //processLot.bagMaterialCode = bagItem.bagMaterialCode
        processLot.startTime = it.fgrnGrades.startTime
        processLot.endTime = it.fgrnGrades.endTime
        processLot.grossWeight = convertKgToMT(grossWeight.toString())
        //processLot.grossWeight = grossWeight.toString().trim()
        //processLot.grossWeightMT = convertKgToMT(processLot.grossWeight)
        processLot.bagList = it.bagItems
        processLot.bayNo = ""
        processLot.customerCode=""
        processList.add(processLot)
        poReq.processingLotDtls = processList
    }

    return poReq
}

fun prepareRminCreatePoRequest(
    stageFevor: String, cfgNumber: String,
    materialName: String,
    materialNo: String,
    model: VegaCoffeeRminProcessing,
    lotDetails: ArrayList<VegaCoffeeRminLots>,
    workFlowData : WorkflowFields,
): VegaNigeriaProcessingCreatePoReq {
    val poReq = VegaNigeriaProcessingCreatePoReq()
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = materialNo
    poReq.materialCode = model.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.all = true
    poReq.poQuantity = model.poQuantity
    poReq.versionId = model.versionId
    poReq.notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0")
    poReq.nextWorkFlowRole = workFlowData?.workflowRole
    poReq.navId = workFlowData?.workflowId
    poReq.currentWorkFlowRole = workFlowData?.module
    poReq.environment = BuildConfig.BUILD_TYPE
    val processList = mutableListOf<VegaNigeriaProcessingRMINLotDetails>()
    lotDetails.forEach {
        val processLot = VegaNigeriaProcessingRMINLotDetails()
        processLot.batchNumber = it.batchNumber
        processLot.materialCode = it.materialCode
        processLot.plant = it.plantId
        val storeCode = it.storageLocationCode?.split("-")
        processLot.storageLocationCode = storeCode?.get(0)
        processLot.unitsOfMeasure = it.unitOfMeasure
        if (processLot.unitsOfMeasure.equals("MT")) {
            processLot.netWeight =
                if (it.editedWeight?.isNotEmpty()!!) (it.editedWeight) else (
                    it.weight
                )
            processLot.menge =
                if (it.editedWeight?.isNotEmpty()!!) (it.editedWeight) else (
                    it.weight
                )
        } else {
            processLot.netWeight =
                if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
            processLot.menge = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
        }
        processLot.bagCount = it.noOfBags
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.deliveryItem
        processLot.movementType = model.bwart
        processLot.phase = model.phase
        processLot.processOrderNum = it.poNumber
        processLot.rsnum = model.rsnum
        processLot.rspos = model.rspos
        processLot.xchpf = model.xchpf
        processLot.resource = model.resource
        processLot.shiftType = model.shift ?: ""
        processLot.remarks = model.remark ?: ""
        processList.add(processLot)
    }
    poReq.processingLotDtls = processList
    return poReq
}

