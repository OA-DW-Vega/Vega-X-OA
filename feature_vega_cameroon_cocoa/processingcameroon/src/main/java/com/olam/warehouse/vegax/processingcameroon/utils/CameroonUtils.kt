package com.olam.warehouse.vegax.processingcameroon.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.CameroonProcessingFgrnLotDetails
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingCreatePoReq
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingFgrnPost
import com.olam.warehouse.vegax.processingcameroon.data.domain.model.VegaCameroonProcessingRMINLotDetails
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

const val RMIN = "rmin"
const val FGRN = "fgrn"
const val ADDLOT = "AddLot"
const val EDITLOT = "EditLot"
const val Lot_List = "LotList"
const val MODEL_BUNDLE = "model"
const val SHIFT = "Shift"
const val SUMMARY = "Summary"
const val FROM_SUMMARY = "fromSummary"
const val FRAG_ITEM = "frag_item"
const val FRAG_ID = "frag_id"
const val FRAG_GRADES = "frag_grades"
const val RMIN_GRADES = "rmin_grades"
const val FRAG_PENDING = "frag_pending"
const val FRAG_ADD_WEIGHT = "frag_add_weight"
const val FRAG_ADD_WEIGHT_EDIT = "frag_add_weight_edit"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val UPDATE_WEIGHT = "UpdateWeight"
const val BATCH_LIST = "batch_list"

//const val JSON_PROCESSING_TYPE_LIST = "PROCESSING_TYPE_LIST"
const val JSON_PROCESSING_TYPE_LIST_EN = "PROCESSING_TYPE_LIST_EN"
const val JSON_PROCESSING_TYPE_LIST_FR = "PROCESSING_TYPE_LIST_FR"

const val JSON_SHIFT_DETAILS_LIST = "SHIFT_DETAILS_LIST"
const val JSON_SHIFT_DETAILS_LIST_EN = "SHIFT_DETAILS_LIST_EN"
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
                "MT" -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble().times(1000))
                else -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
            }
        }
    }

    rfgrnList?.forEach { fgrn ->
        if (!fgrn.netWeight.isNullOrEmpty()) {
            when (fgrn.unitsOfMeasure) {
                "KG" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())
                "MT" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble().times(1000))
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
    return weight?.toDouble()?.div(1000)?.formatThreeDigits()
}

fun processToProcessingMaterial(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeFgrnGradesMatrialWeights {
    val data = VegaCoffeeFgrnGradesMatrialWeights()
    data.id = bagMaterial.id
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight
    data.bagType = bagMaterial.bagType
    data.bagType1 = bagMaterial.bagType1
    data.bagCount = bagMaterial.bagCount
    data.bagCount1 = bagMaterial.bagCount1
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.bagMaterialCode1 = bagMaterial.bagMaterialCode1
    data.tareWeight = bagMaterial.tareWeight
    data.tareWeight1 = bagMaterial.tareWeight1
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
    data.bagType1 = bagMaterial.bagType1
    data.bagCount = bagMaterial.bagCount
    data.bagCount1 = bagMaterial.bagCount1
    data.tareWeight = bagMaterial.tareWeight
    data.tareWeight1 = bagMaterial.tareWeight1
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.bagMaterialCode1 = bagMaterial.bagMaterialCode1
    data.palletAverage = bagMaterial.palletAverage
    return data
}

fun prepareRminPostRequest(
    poOrderDetails: VegaCoffeeRminProcessing?,
    lotDetails: VegaCoffeeRminItemWithGrades
): VegaCameroonProcessingFgrnPost {

    val poReq = VegaCameroonProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remark
    poReq.shiftType = poOrderDetails?.shift
    poReq.rmin = true
    val processList = mutableListOf<CameroonProcessingFgrnLotDetails>()

    lotDetails.gradeWithLotsAndBags?.forEach {
        val grade = it.rminGrades
        it.lotItems?.forEach {
            val processLot = CameroonProcessingFgrnLotDetails()
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
                bagSort.addAll(filterList)
            }
            it.lotBagItems = bagSort.asReversed()
            filterList?.forEach{
                when (it.unitsOfMeasure) {
                    "KG" -> {
                        it.grossWeight = (it.grossWeight)
                        it.netWeight = (it.netWeight)!!
                        it.tareWeight = (it.tareWeight)!!
                        it.tareWeight1 = (it.tareWeight1)!!
                        it.unitsOfMeasure = "KG"
                        it.palletWeight = (it.palletWeight)!!
                        it.palletAverage = (it.palletAverage)!!
                    }
                    "MT" -> {
                        it.grossWeight = convertKgToMT(it.grossWeight)!!
                        it.netWeight = convertKgToMT(it.netWeight)!!
                        it.tareWeight = convertKgToMT(it.tareWeight)!!
                        it.tareWeight1 = convertKgToMT(it.tareWeight1)!!
                        it.unitsOfMeasure = "MT"
                        it.palletWeight = convertKgToMT(it.palletWeight)!!
                        it.palletAverage = convertKgToMT(it.palletAverage)!!
                    }
                }
            }
            it.lotBagItems?.forEach { item ->
                val palletAvg =
                    if (!item.noOfPallet.isNullOrEmpty() && item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                        ?.div(item.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                tareWeight =
                    tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                        .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
                        .plus(palletAvg!!)
                bagTareWeight = bagTareWeight.plus(
                    item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!
                ).plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
            }
            when (it.rminLots.meins) {
                "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
//            processLot.netWeight = convertKgToMT(netWeight.toString())
            processLot.bagCount = it.lotBagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
            processLot.bagCount1 =
                it.lotBagItems?.sumBy { it1 -> it1.bagCount1!!.toInt() }.toString()
            processLot.bagType = it.lotBagItems!![0].bagType
            processLot.bagType1 = it.lotBagItems!![0].bagType1
            processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
            processLot.huno = it.lotBagItems!![0].unitsOfMeasure
            when (it.lotBagItems!![0].unitsOfMeasure) {
                "KG" ->
                    processLot.huwt = (bagTareWeight.toString()).trim()
                "MT" ->
                    processLot.huwt = convertKgToMT(bagTareWeight.toString())?.trim()
            }
            processLot.huno2 = "KG"
            processLot.huwt2 = it.lotBagItems!![0].palletAverage
            processLot.nohu1 =
                it.lotBagItems?.sumBy { tar -> tar.bagCount.toInt().plus(tar.bagCount1!!.toInt()) }
                    .toString().trim()
            processLot.nohu2 = it.lotBagItems!![0].noOfPallet
            processLot.netWeight = netWeight.toString()
            processLot.menge = netWeight.toString()
            processLot.storageLocationCode = it.rminLots.storageLocationCode
            processLot.unitsOfMeasure = grade.meins
            processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.deliveryItem = grade.deliveryItem
            processLot.menge = grade.weight
            processLot.movementType = grade.bwart
            processLot.phase = grade.phase
            processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
            processLot.processOrderNum = grade.processOrderNo
            processLot.rsnum = grade.rsNum
            processLot.rspos = grade.rsPos
            processLot.xchpf = grade.xchpf
            processLot.storageLossFlag = it.rminLots.storageLossFlag
            processLot.bagMaterialCode = it.lotBagItems!![0].bagMaterialCode
            processLot.bagMaterialCode1 = it.lotBagItems!![0].bagMaterialCode1
            processLot.startTime = grade.startTime
            processLot.endTime = grade.endTime
            processLot.grossWeight = grossWeight.toString().trim()
            processLot.bagList = it.lotBagItems
            processList.add(processLot)
        }
    }
    poReq.processingLotDtls = processList
    return poReq
}


fun prepareFgrnPostRequest(
    poOrderDetails: VegaCoffeeFgrnItems?,
    lotDetails: List<VegaCoffeeFgrnGradesWithBagItems>?,
    sapMaterialList: ArrayList<VegaMaterial> = ArrayList<VegaMaterial>()
): VegaCameroonProcessingFgrnPost {

    val poReq = VegaCameroonProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.materialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remarks
    poReq.shiftType = poOrderDetails?.shiftSelection
    val processList = mutableListOf<CameroonProcessingFgrnLotDetails>()
    var complaintMaterial= ArrayList<VegaMaterial>()
    var nonComplaintMaterial= ArrayList<VegaMaterial>()

    //sapMaterial list only applicable for T&T here
    if(sapMaterialList.isNotEmpty()) {
        complaintMaterial = sapMaterialList.filter { it.complainceFlag == Constants.COMPLAINT } as ArrayList<VegaMaterial>
        nonComplaintMaterial = sapMaterialList.filter { it.complainceFlag == Constants.NON_COMPLAINT } as ArrayList<VegaMaterial>
    }
    lotDetails?.forEach { it ->
        val processLot = CameroonProcessingFgrnLotDetails()
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
                    it.tareWeight1 = it.tareWeight1
//                   it.tareWeight = (it.tareWeight)!!
                    it.unitsOfMeasure = "KG"
                    it.palletWeight = (it.palletWeight)!!
                    it.palletAverage = (it.palletAverage)!!
                }
                "MT" -> {
                    it.grossWeight = convertKgToMT(it.grossWeight)!!
                    it.netWeight = convertKgToMT(it.netWeight)!!
                    it.tareWeight = it.tareWeight
                    it.tareWeight1 = it.tareWeight1
//                   it.tareWeight = convertKgToMT(it.tareWeight)!!
                    it.unitsOfMeasure = "MT"
                    it.palletWeight = convertKgToMT(it.palletWeight)!!
                    it.palletAverage = convertKgToMT(it.palletAverage)!!
                }

            }

        }
        it.bagItems?.forEach { item ->
            val palletAvg = if (item.noOfPallet?.isNotEmpty() == true)
                if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()?.div(item.noOfPallet?.toInt()!!) else 0.0
            else 0.0
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight =
                tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
                    .plus(palletAvg!!)
            bagTareWeight =
                bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                    .plus(item.tareWeight1?.toDouble()?.times(item.bagCount1!!.toDouble())!!)
        }
        when (it.fgrnGrades.meins) {
            "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
            "KG" -> netWeight = grossWeight.minus(tareWeight)
            else -> netWeight = grossWeight.minus(tareWeight)
        }
        processLot.netWeight = netWeight.toString()
        processLot.vendorCode = poOrderDetails?.vendor
        processLot.storageLocationCode = it.fgrnGrades.lotStorageLocationCode
        processLot.unitsOfMeasure = it.fgrnGrades.meins
        processLot.bagCount = it.bagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
        processLot.bagCount1 = it.bagItems?.sumBy { it1 -> it1.bagCount1!!.toInt() }.toString()
        processLot.bagType = it.bagItems!![0].bagType
        processLot.bagType1 = it.bagItems!![0].bagType1
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
        processLot.nohu1 =
            it.bagItems?.sumBy { tar -> tar.bagCount.toInt().plus(tar.bagCount1!!.toInt()) }
                .toString().trim()
        processLot.nohu2 = it.bagItems!![0].noOfPallet
        processLot.bagMaterialCode = it.bagItems!![0].bagMaterialCode
        processLot.bagMaterialCode1 = it.bagItems!![0].bagMaterialCode1
        processLot.startTime = it.fgrnGrades.startTime
        processLot.endTime = it.fgrnGrades.endTime
        processLot.grossWeight = grossWeight.toString().trim()
        processLot.bagList = it.bagItems
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
    lotDetails: ArrayList<VegaCoffeeRminLots>
): VegaCameroonProcessingCreatePoReq {
    val poReq = VegaCameroonProcessingCreatePoReq()
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = materialNo
    poReq.materialCode = model.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = model.versionId
    val processList = mutableListOf<VegaCameroonProcessingRMINLotDetails>()
    lotDetails.forEach {
        val processLot = VegaCameroonProcessingRMINLotDetails()
        processLot.batchNumber = it.batchNumber
        processLot.materialCode = it.materialCode
        processLot.plant = it.plantId
        processLot.netWeight = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
        val storeCode = it.storageLocationCode?.split("-")
        processLot.storageLocationCode = storeCode?.get(0)
        processLot.unitsOfMeasure = it.unitOfMeasure
        processLot.bagCount = it.noOfBags
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.deliveryItem
        processLot.menge = if (it.editedWeight?.isNotEmpty()!!) it.editedWeight else it.weight
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
