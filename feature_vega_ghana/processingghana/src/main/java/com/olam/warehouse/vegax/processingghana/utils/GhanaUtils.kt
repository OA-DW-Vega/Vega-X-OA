package com.olam.warehouse.vegax.processingghana.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeRminItemWithGrades
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatNDigits
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.data.domain.model.GhanaProcessingFgrnLotDetails
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingCreatePoReq
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnPost
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingRMINLotDetails
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

const val RMIN = "rmin"
const val FGRN = "fgrn"
const val POBOM = "PoBom"
const val ADDLOT = "AddLot"
const val EDITLOT = "EditLot"
const val Lot_List = "LotList"
const val MODEL_BUNDLE = "model"
const val SHIFT = "Shift"
const val SUMMARY = "Summary"
const val FROM_SUMMARY = "fromSummary"
const val FROM_POSELECTION = "fromposelection"
const val FRAG_ITEM = "frag_item"
const val FRAG_ID = "frag_id"
const val VEGA_STAGE = "vega_stage"
const val BAG_ITEM = "vega_bag_item"
const val FRAG_GRADES = "frag_grades"
const val RMIN_GRADES = "rmin_grades"
const val FRAG_PENDING = "frag_pending"
const val FRAG_ADD_WEIGHT = "frag_add_weight"
const val FRAG_RMIN_LIST = "frag_rmin_list"
const val FRAG_ADD_WEIGHT_EDIT = "frag_add_weight_edit"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val UPDATE_WEIGHT = "UpdateWeight"
const val BATCH_LIST = "batch_list"
const val OFFLINE_RMIN_LOTS = "rmin_lots"
const val OFFLINE_RMIN_ITEMS = "rmin_items"
const val PO_NO = "rmin_po"
const val STAGE = "rmin_stage"

const val RMIN_OFFLINE = "rmin_offline"
const val FGRN_OFFLINE = "fgrn_offline"

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

const val OFFLINE_RMIN_SUMMARY_FRAG = "offline summary"
const val OFFLINE_FGRN_SUMMARY_FRAG = "fgrn_offline_summary"

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
    var bagWeight = 0.0
    var totalRminWeight = 0.0
    rminList?.forEach { rmin ->
        if (!rmin.netWeight.isNullOrEmpty()) {
            when (rmin.unitsOfMeasure) {
                "KG" -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
                else -> rminWeight = rminWeight.plus(rmin.netWeight!!.toDouble())
            }
        }
        if (!rmin.netWeight.isNullOrEmpty()) {
            if (rmin.materialCode!!.startsWith("0000002"))
                bagWeight = bagWeight.plus(rmin.netWeight!!.toDouble())
//            else
//                bagWeight = 0.0
        }
        totalRminWeight = rminWeight - bagWeight

    }

    rfgrnList?.forEach { fgrn ->
        if (!fgrn.netWeight.isNullOrEmpty()) {
            when (fgrn.unitsOfMeasure) {
                "KG" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())
//                "MT" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble().times(1000))
                else -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight!!.toDouble())

            }
        }
    }
    return when (uom) {
        "KG" -> totalRminWeight - fgrnWeight
        else -> totalRminWeight - fgrnWeight
    }
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

fun processToProcessingFGRNMaterial(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeFgrnGradesMatrialWeights {
    val data = VegaCoffeeFgrnGradesMatrialWeights()
    data.id = bagMaterial.id
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight
    data.bagType = bagMaterial.bagType
    data.totalBagCount = bagMaterial.bagCount
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

fun processingMaterialToProcessFgrn(bagMaterial: VegaCoffeeFgrnGradesMatrialWeights): VegaCocoaSweepingBagMaterial {
    val data = VegaCocoaSweepingBagMaterial()
    data.id = bagMaterial.id
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight.toString()
    data.bagType = bagMaterial.bagType
    data.bagCount = bagMaterial.totalBagCount!!
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
    lotDetails: VegaCoffeeRminItemWithGrades
): VegaGhanaProcessingFgrnPost {
    val poReq = VegaGhanaProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remark
    poReq.shiftType = poOrderDetails?.shift
    poReq.rmin = true
    val processList = mutableListOf<GhanaProcessingFgrnLotDetails>()

    lotDetails.gradeWithLotsAndBags?.forEach {
        val grade = it.rminGrades
        it.lotItems?.forEach {
            val processLot = GhanaProcessingFgrnLotDetails()
            val batch = it.rminLots.batchNumber
            processLot.batchNumber = it.rminLots.batchNumber
            processLot.materialCode = it.rminLots.materialCode
            processLot.plant = it.rminLots.plantId
            poOrderDetails?.lotList?.forEach { lot ->
                if (lot.batchNumber == batch) {
//                    processLot.grossWeight = lot.remarks
                    processLot.grossWeight = lot.editedWeight
                    processLot.netWeight = lot.editedWeight
                    processLot.menge = lot.editedWeight
                }
            }
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
            filterList?.forEach {
                it.grossWeight = (it.grossWeight).toDouble().div(1000).toString()
                it.netWeight = (it.netWeight)?.toDouble()?.div(1000).toString()
                it.tareWeight = (it.tareWeight)?.toDouble()?.div(1000)?.formatNDigits(6)
                it.unitsOfMeasure = "MT"
                it.palletWeight = it.palletWeight!!
                it.palletAverage = it.palletAverage!!
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
//                "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
                "MT" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
//            processLot.netWeight = convertKgToMT(netWeight.toString())
            processLot.bagCount = it.lotBagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
//            processLot.bagType = it.lotBagItems!![0].bagType
            processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
            processLot.huno = "MT"
            processLot.huwt = bagTareWeight.toString().trim()
            processLot.huno2 = "MT"
            processLot.huwt2 = "0"
            processLot.nohu1 = it.lotBagItems?.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
            processLot.nohu2 = "0"
//            processLot.netWeight = netWeight.formatThreeDigits()
            processLot.menge = netWeight.toString()
            processLot.storageLocationCode = it.rminLots.storageLocationCode
            processLot.unitsOfMeasure = grade.meins
            processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
            processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
            processLot.deliveryItem = grade.deliveryItem
//            processLot.menge = grade.weight
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
    lotDetails: List<VegaCoffeeFgrnGradesWithBagItems>?
): VegaGhanaProcessingFgrnPost {
    var ppBagCode = ""
    val poReq = VegaGhanaProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.materialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.remarks
    poReq.shiftType = poOrderDetails?.shiftSelection
    val processList = mutableListOf<GhanaProcessingFgrnLotDetails>()
    lotDetails?.forEach { it ->
        val processLot = GhanaProcessingFgrnLotDetails()
        processLot.batchNumber = it.fgrnGrades.batchNumber
        processLot.materialCode = it.fgrnGrades.materialCode
        processLot.plant = it.fgrnGrades.plantId
        it.bagItems?.forEach {
            processLot.bagCount = it.bagCount
            if(it.bagCount == "0"){
                processLot.bagType = ""
                processLot.bagMaterialCode = ""
            }
            else {
                processLot.bagType = it.bagType
                processLot.bagMaterialCode = it.bagMaterialCode
            }
            processLot.netWeight = it.netWeight
            processLot.menge = it.netWeight
            processLot.grossWeight = it.netWeight
        }

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
            if (it.bagMaterialCode == "000000200000000132") {
                ppBagCode = "000000200000000132"
            }
            it.grossWeight = it.grossWeight
            it.netWeight = it.netWeight!!
//            it.tareWeight = it.tareWeight
            it.tareWeight = (it.tareWeight)?.toDouble()?.div(1000)?.formatNDigits(6)
            it.unitsOfMeasure = "MT"
            it.palletWeight = it.palletWeight!!
            it.palletAverage = it.palletAverage!!
            it.materialCode = it.bagMaterialCode
            it.bagMaterialCode = it.bagMaterialCode
        }
        it.bagItems?.forEach { item ->
            val palletAvg =
                if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                    ?.div(item.noOfPallet?.toInt()!!) else 0.0
            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
                .plus(palletAvg!!)
            bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
        }
        when (it.fgrnGrades.meins) {
//            "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
            "KG" -> netWeight = grossWeight.minus(tareWeight)
            else -> netWeight = grossWeight.minus(tareWeight)
        }
        processLot.vendorCode = poOrderDetails?.vendor
        processLot.storageLocationCode = it.fgrnGrades.lotStorageLocationCode
        processLot.unitsOfMeasure = it.fgrnGrades.unitOfMeasure
        processLot.year = Calendar.getInstance().get(Calendar.YEAR).toString()
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.fgrnGrades.deliveryItem
        processLot.movementType = it.fgrnGrades.bwart
        processLot.phase = it.fgrnGrades.phase
        processLot.plant = PreferenceHelper.get(Constants.WERKS, "")
        processLot.processOrderNum = it.fgrnGrades.processOrderNo
        processLot.rsnum = it.fgrnGrades.rsNum
        processLot.rspos = it.fgrnGrades.rsPos
        processLot.xchpf = it.fgrnGrades.xchpf
        processLot.huno = "MT"
        processLot.huwt = bagTareWeight.toString().trim()
        processLot.huno2 = "MT"
        processLot.huwt2 = "0"
        processLot.nohu1 = it.bagItems?.sumBy { tar -> tar.totalBagCount?.toInt() ?: 0 }.toString().trim()
        processLot.nohu2 = "0"
        //processLot.bagMaterialCode = bagItem.bagMaterialCode
        processLot.startTime = it.fgrnGrades.startTime
        processLot.endTime = it.fgrnGrades.endTime
//        processLot.bagList = it.bagItems
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
): VegaGhanaProcessingCreatePoReq {
    val poReq = VegaGhanaProcessingCreatePoReq()
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = model.baseMaterialCode
    poReq.materialCode = materialNo
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = model.versionId
    val processList = mutableListOf<VegaGhanaProcessingRMINLotDetails>()
    lotDetails.forEach {
        val processLot = VegaGhanaProcessingRMINLotDetails()
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

fun prepareRminofflineCreatePoRequest(
    stageFevor: String, cfgNumber: String,
    materialName: String,
    materialNo: String,
    model: VegaGhanaProcessingFgrnPost, versionID: String
): VegaGhanaProcessingCreatePoReq {
    val poReq = VegaGhanaProcessingCreatePoReq()
    poReq.fgrn = true
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = ""
    poReq.outputMaterialCode = materialNo
    poReq.materialCode = materialNo
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = versionID
    val processList = mutableListOf<VegaGhanaProcessingRMINLotDetails>()
    model.processingLotDtls.forEach {
        val processLot = VegaGhanaProcessingRMINLotDetails()
        processLot.batchNumber = it.batchNumber
        processLot.materialCode = it.materialCode
        processLot.plant = getPlantDetails().plantId
        processLot.netWeight = it.netWeight
        val storeCode = it.storageLocationCode?.split("-")
        processLot.storageLocationCode = storeCode?.get(0)
        processLot.unitsOfMeasure = it.unitsOfMeasure
        processLot.bagCount = it.bagCount
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.deliveryItem
        processLot.menge = it.netWeight
        processLot.movementType = it.movementType
        processLot.phase = ""
        processLot.processOrderNum = it.processOrderNum
        processLot.rsnum = it.rsnum
        processLot.rspos = it.rspos
        processLot.xchpf = it.xchpf
        processLot.resource = ""
        processLot.shiftType = ""
        processLot.remarks = ""
        processList.add(processLot)
    }
    poReq.processingLotDtls = processList
    return poReq
}

fun prepareOfflineRminPostRequest(list: VegaGhanaProcessingFgrnPost, id: String): VegaGhanaOfflineRminData
{
    val rminList = VegaGhanaOfflineRminData()
    rminList.rminTempId = id
    rminList.outputMaterialCode = list.outputMaterialCode
    rminList.plant = list.plant.toString()
    rminList.processingStage = list.processingStage
    rminList.rmin = true
    rminList.shiftType = ""
    rminList.versionId = list.versionId
    return rminList
}

fun prepareOfflineRminBomRequest(
    list: VegaGhanaProcessingCreatePoReq,
    id: String
): VegaGhanaOfflineRminData {
    val rminList = VegaGhanaOfflineRminData()
    rminList.rminTempId = id
    rminList.outputMaterialCode = list.outputMaterialCode
    rminList.plant = list.plant.toString()
    rminList.processingStage = list.processingStage
    rminList.operatorName = list.cfgNo
    rminList.rmin = true
    rminList.shiftType = ""
    rminList.versionId = list.versionId
    return rminList
}

fun prepareOfflineFgrnPostRequest(
    list: VegaGhanaProcessingFgrnPost,
    id: String,
    rminId: String,
    rminQty: String?
): VegaGhanaOfflineFgrnData {
    val rminList = VegaGhanaOfflineFgrnData()
    rminList.fgrnTempId = id
    rminList.rminTempId = rminId
    rminList.outputMaterialCode = list.outputMaterialCode
    rminList.plant = list.plant.toString()
    rminList.processingStage = list.processingStage
    rminList.rmin = true
    rminList.shiftType = ""
    rminList.versionId = list.versionId
    rminList.rminQty = rminQty
    return rminList
}

fun prepareOfflineRminProcessLotDetails(
    list: GhanaProcessingFgrnLotDetails,
    id: String,
    po: String,
    stage: String,
    materialName: String
): VegaGhanaOfflineRminProcessLotDetails {
    val processList = VegaGhanaOfflineRminProcessLotDetails()
    processList.rminTempId = id
    processList.batchNumber = list.batchNumber!!
    processList.bagCount = list.bagCount
    processList.confText = list.confText
    processList.deliveryItem = list.deliveryItem
    processList.materialCode = list.materialCode
    processList.menge = list.menge
    processList.movementType = list.movementType
    processList.netWeight = list.netWeight
    processList.grossWeight = list.grossWeight
    processList.startTime = "/Date(".plus(DateUtils.getCurrentTimeInMills().toString()).plus(")/")
    processList.endTime = list.endTime
    processList.phase = list.phase
    processList.plant = list.plant
    processList.bagType = list.bagType
    processList.year = list.year
    processList.processOrderNum = list.processOrderNum
    processList.rsnum = list.rsnum
    processList.rspos = list.rspos
    processList.storageLocationCode = list.storageLocationCode
    processList.unitsOfMeasure = list.unitsOfMeasure
    processList.xchpf = list.xchpf
    processList.huno = list.huno
    processList.huwt = list.huwt
    processList.huno2 = list.huno2
    processList.huwt2 = list.huwt2
    processList.nohu1 = list.nohu1
    processList.nohu2 = list.nohu2
    processList.bagMaterialCode = list.bagMaterialCode
    processList.endLotFlag = false
    processList.vendorCode = list.vendorCode
    processList.storageLossFlag = false
    processList.poNo = po
    processList.stage = stage
    processList.materialName = materialName
    return processList
}

fun prepareOfflineRminBomLotDetails(
    list: VegaGhanaProcessingRMINLotDetails,
    id: String,
    po: String,
    stage: String,
    materialName: String
): VegaGhanaOfflineRminProcessLotDetails {
    val processList = VegaGhanaOfflineRminProcessLotDetails()
    processList.rminTempId = id
    processList.batchNumber = list.batchNumber!!
    processList.bagCount = if (list.bagCount.isNullOrEmpty()) "0" else list.bagCount
    processList.confText = list.confText
    processList.deliveryItem = list.deliveryItem
    processList.materialCode = list.materialCode
    processList.menge = list.menge
    processList.movementType = list.movementType
    processList.netWeight = list.netWeight
    processList.grossWeight = ""
    processList.startTime = "/Date(".plus(DateUtils.getCurrentTimeInMills().toString()).plus(")/")
    processList.endTime = ""
    processList.phase = list.phase
    processList.plant = list.plant
    processList.bagType = ""
    processList.year = ""
    processList.processOrderNum = list.processOrderNum
    processList.rsnum = list.rsnum
    processList.rspos = list.rspos
    processList.storageLocationCode = list.storageLocationCode
    processList.unitsOfMeasure = list.unitsOfMeasure
    processList.xchpf = list.xchpf
    processList.huno = list.unitsOfMeasure
    processList.huwt = "0.0"
    processList.huno2 = list.unitsOfMeasure
    processList.huwt2 = "0"
    processList.nohu1 = "0"
    processList.nohu2 = "0"
    processList.bagMaterialCode = ""
    processList.endLotFlag = false
    processList.vendorCode = ""
    processList.storageLossFlag = false
    processList.poNo = po
    processList.stage = stage
    processList.materialName = materialName
    return processList
}

fun prepareOfflineFgrnProcessLotDetails(
    list: GhanaProcessingFgrnLotDetails,
    id: String,
    po: String,
    stage: String,
    materialName: String,
    temRminId: String
): VegaGhanaOfflineFgrnProcessLotDetails {
    val processList = VegaGhanaOfflineFgrnProcessLotDetails()
    processList.fgrnTempId = id
    processList.rminTempId = temRminId
    processList.batchNumber = list.batchNumber!!
    processList.bagCount = list.bagCount
    processList.confText = list.confText
    processList.deliveryItem = list.deliveryItem
    processList.materialCode = list.materialCode
    processList.menge = list.menge
    processList.movementType = list.movementType
    processList.netWeight = list.netWeight
    processList.grossWeight = list.grossWeight
    processList.startTime = list.startTime
    processList.endTime = list.endTime
    processList.phase = list.phase
    processList.plant = list.plant
    processList.bagType = list.bagType
    processList.year = list.year
    processList.processOrderNum = list.processOrderNum
    processList.rsnum = list.rsnum
    processList.rspos = list.rspos
    processList.storageLocationCode = list.storageLocationCode
    processList.unitsOfMeasure = list.unitsOfMeasure
    processList.xchpf = list.xchpf
    processList.huno = list.huno
    processList.huwt = list.huwt
    processList.huno2 = list.huno2
    processList.huwt2 = list.huwt2
    processList.nohu1 = list.nohu1
    processList.nohu2 = list.nohu2
    processList.bagMaterialCode = list.bagMaterialCode
    processList.endLotFlag = false
    processList.vendorCode = list.vendorCode
    processList.storageLossFlag = false
    processList.poNo = po
    processList.stage = stage
    processList.materialName = materialName
    return processList
}

fun prepareLotList(lotList: VegaCoffeeRminLots, tempId: String): VegaGhanaOfflineRminLots {
    val rminLots = VegaGhanaOfflineRminLots()
    rminLots.batchNumber = lotList.batchNumber
    rminLots.rminTempId = tempId
    rminLots.storageLocationCode = lotList.storageLocationCode
    rminLots.materialName = lotList.materialName
    rminLots.weight = lotList.weight
    rminLots.editedWeight = lotList.editedWeight
    return rminLots
}

fun prepareItems(itemList: VegaCoffeeFgrnItemsGrades, tempId: String): VegaGhanaOfflineRminItems {
    val rminList = VegaGhanaOfflineRminItems()
    rminList.materialCode = itemList.materialCode
    rminList.rminTempId = tempId
    rminList.weightToProcess = itemList.weightToProcess
    rminList.materialName = itemList.materialName
    return rminList
}

fun prepareSummaryOfflineFgrnPostRequest(
    fgrnItem: ArrayList<VegaGhanaOfflineFgrnProcessLotDetails>,
    vegaStage: ArrayList<VegaGhanaOfflineFgrnData>,
    rminList: List<VegaGhanaOfflineRminProcessLotDetails>
): VegaGhanaProcessingFgrnPost {
    val fgrnPost = VegaGhanaProcessingFgrnPost()
    var processingLots = ArrayList<GhanaProcessingFgrnLotDetails>()
    rminList.forEach {
        var rmin = GhanaProcessingFgrnLotDetails()
        rmin.bagCount = it.bagCount
        rmin.batchNumber = it.batchNumber
        rmin.confText = it.confText
        rmin.deliveryItem = it.deliveryItem
        rmin.materialCode = it.materialCode
        rmin.menge = it.netWeight
        rmin.movementType = it.movementType
        rmin.netWeight = it.netWeight
        rmin.grossWeight = it.netWeight
        rmin.startTime = it.startTime
        rmin.endTime = it.endTime
        rmin.phase = it.phase
        rmin.plant = it.plant
        rmin.year = it.year
        rmin.processOrderNum = it.processOrderNum
        rmin.rsnum = it.rsnum
        rmin.rspos = it.rspos
        rmin.storageLocationCode = it.storageLocationCode
        rmin.unitsOfMeasure = it.unitsOfMeasure
        rmin.xchpf = it.xchpf
        rmin.huno = it.huno
        rmin.huwt = it.huwt
        rmin.huno2 = it.huno2
        rmin.huwt2 = it.huwt2
        rmin.nohu1 = it.nohu1
        rmin.nohu2 = it.nohu2
        rmin.endLotFlag = it.endLotFlag!!
        rmin.vendorCode = it.vendorCode
        rmin.storageLossFlag = it.storageLossFlag!!
        rmin.bagMaterialCode = it.bagMaterialCode
        rmin.bagType = it.bagType
        processingLots.add(rmin)
    }

    fgrnItem.forEach {
        var lotDts = GhanaProcessingFgrnLotDetails()
        if (it.bagCount == "0") {
            lotDts.bagMaterialCode = ""
            lotDts.bagType = ""
        } else {
            lotDts.bagMaterialCode = it.bagMaterialCode
            lotDts.bagType = it.bagType
        }
        lotDts.bagCount = it.bagCount
        lotDts.batchNumber = it.batchNumber
        lotDts.confText = it.confText
        lotDts.deliveryItem = it.deliveryItem
        lotDts.materialCode = it.materialCode
        lotDts.menge = it.netWeight
        lotDts.movementType = it.movementType
        lotDts.netWeight = it.netWeight
        lotDts.grossWeight = it.netWeight
        lotDts.startTime = it.startTime
        lotDts.endTime = it.endTime
        lotDts.phase = it.phase
        lotDts.plant = it.plant
        lotDts.year = it.year
        lotDts.processOrderNum = it.processOrderNum
        lotDts.rsnum = it.rsnum
        lotDts.rspos = it.rspos
        lotDts.storageLocationCode = it.storageLocationCode
        lotDts.unitsOfMeasure = it.unitsOfMeasure
        lotDts.xchpf = it.xchpf
        lotDts.huno = it.huno
        lotDts.huwt = it.huwt
        lotDts.huno2 = it.huno2
        lotDts.huwt2 = it.huwt2
        lotDts.nohu1 = it.nohu1
        lotDts.nohu2 = it.nohu2
        lotDts.endLotFlag = it.endLotFlag!!
        lotDts.vendorCode = it.vendorCode
        lotDts.storageLossFlag = it.storageLossFlag!!
        processingLots.add(lotDts)
    }

    vegaStage.forEach {
        fgrnPost.processingStage = it.processingStage
        fgrnPost.key = getCurrentKey()
        fgrnPost.outputMaterialCode = it.outputMaterialCode
        fgrnPost.plant = getPlantDetails()
        fgrnPost.processingLotDtls = processingLots
        fgrnPost.rmin = false
        fgrnPost.shiftType = ""
        fgrnPost.operatorName = it.operatorName
        fgrnPost.versionId = it.versionId
    }
    return fgrnPost
}



