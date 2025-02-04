package com.olam.warehouse.vegax.processingindo.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.CocoaProcessingFgrnLotDetails
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindo.data.domain.model.VegaProcessingIndoCreatePoReq
import com.olam.warehouse.vegax.processingindo.data.domain.model.VegaProcessingIndoRMINLotDetails
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */

const val RMIN = "rmin"
const val FGRN = "fgrn"
const val POBOM = "PoBom"
const val ADDLOT = "AddLot"
const val Lot_List = "LotList"
const val MODEL_BUNDLE = "model"
const val SHIFT = "Shift"
const val SUMMARY = "Summary"
const val FROM_SUMMARY = "fromSummary"
const val FRAG_ITEM = "frag_item"
const val FRAG_ID = "frag_id"
const val FRAG_GRADES = "frag_grades"
const val FRAG_PENDING = "frag_pending"
const val FRAG_ADD_WEIGHT = "frag_add_weight"
const val FRAG_ADD_WEIGHT_EDIT = "frag_add_weight_edit"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val SEIVING = "Sieving Process"
const val ROUND_OFF = "Round_Off"

//const val JSON_PROCESSING_TYPE_LIST = "PROCESSING_TYPE_LIST"
const val JSON_PROCESSING_TYPE_LIST_EN = "PROCESSING_TYPE_LIST_EN"
const val JSON_PROCESSING_TYPE_LIST_FR = "PROCESSING_TYPE_LIST_FR"

//const val JSON_SHIFT_DETAILS_LIST = "SHIFT_DETAILS_LIST"
const val JSON_SHIFT_DETAILS_LIST_EN = "SHIFT_DETAILS_LIST_EN"
const val JSON_SHIFT_DETAILS_LIST_FR = "SHIFT_DETAILS_LIST_FR"
const val LANGUAGE = "languageCode"
const val MULTIPLE_LOT = "addMultiple"
const val FRAG_CREATE_LOT = "frag_create_lot"
const val FRAG_SIFFT = "frag_sifft"
const val FRAG_SUMMARY = "frag_summary"
const val FRAG_FILTER = "frag_filter"
const val FRAG_SUMMARY_BACK = "frag_summary_back"
const val MATERIAL_CODE = "material_code"
const val IS_EDIT = "is_edit"
const val FULL_FILTER = "full_filter"
const val FILTER_WH_LOC = "filter_wh_loc"
const val FILTER_START_RANGE = "filter_start_range"
const val FILTER_END_RANGE = "filter_end_range"
const val FILTER_ABOVE_RANGE = "filter_above_range"
const val STORAGE_LOC = "storage_loc"
const val LOT_ID = "lot_id"
const val CREATE_NEW_LOT = "create_new_lot"
const val CREATE_NEW_ID = "create_new_id"
const val STOCK_LIST = "stock_list"
const val INDEX_WEIGHTMENT_TYPE = "index_weighment_type"

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
                "KG" -> rminWeight = rminWeight.plus(rmin.netWeight?.toDouble() ?: 0.0)
                "MT" -> rminWeight = rminWeight.plus(rmin.netWeight?.toDouble()?.times(1000) ?: 0.0)
            }
        }
    }

    rfgrnList?.forEach { fgrn ->
        if (!fgrn.netWeight.isNullOrEmpty()) {
            when (fgrn.unitsOfMeasure) {
                "KG" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight?.toDouble() ?: 0.0)
                "MT" -> fgrnWeight = fgrnWeight.plus(fgrn.netWeight?.toDouble()?.times(1000) ?: 0.0)
            }
        }
    }
    return when (uom) {
        "KG" -> rminWeight - fgrnWeight
        "MT" -> (rminWeight - fgrnWeight).div(1000)
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

fun processToProcessingMaterial(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCocoaFgrnGradesMatrialWeights {
    val data = VegaCocoaFgrnGradesMatrialWeights()
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
    return data
}

fun processingMaterialToProcess(bagMaterial: VegaCocoaFgrnGradesMatrialWeights): VegaCocoaSweepingBagMaterial {
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

fun prepareFgrnPostRequest(
    poOrderDetails: VegaCocoaFgrnItems?,
    lotDetails: List<VegaCocoaFgrnGradesWithBagItems>?, isRoundOff: Boolean, isIndexweighmenttype: Boolean
): VegaCocoaProcessingFgrnPost {
    val poReq = VegaCocoaProcessingFgrnPost()
    poReq.key = getCurrentKey()
    poReq.outputMaterialCode = poOrderDetails?.materialCode
    poReq.plant = getPlantDetails()
    poReq.operatorName = poOrderDetails?.operatorName
    poReq.shiftType = poOrderDetails?.shiftSelection
    val processList = mutableListOf<CocoaProcessingFgrnLotDetails>()
    lotDetails?.forEach { it ->
        val processLot = CocoaProcessingFgrnLotDetails()
        processLot.batchNumber = it.fgrnGrades.batchNumber
        processLot.sequence = it.fgrnGrades.sequence
        processLot.materialCode = it.fgrnGrades.materialCode
        processLot.plant = it.fgrnGrades.plantId
        var grossWeight = 0.0
        var tareWeight = 0.0
        var bagTareWeight = 0.0
        var netWeight = 0.0
        val bagSort = arrayListOf<VegaCocoaFgrnGradesMatrialWeights>()
        val dat = it.bagItems?.sortedByDescending { it.createdTime }
        if (dat != null) {
            bagSort.addAll(dat)
        }
        it.bagItems = bagSort.asReversed()
        it.bagItems?.forEach { item ->
            val palletAvg =
                if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                    ?.div(item.noOfPallet?.toInt()!!) else 0.0

            grossWeight = grossWeight.plus(item.grossWeight.toDouble())
            tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
                .plus(palletAvg!!)
            bagTareWeight = bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!)
            if (it.fgrnGrades.isRoundOff!!) {
                it.fgrnGrades.isRoundOff=true
                item.netWeight = bagTareWeight.roundToInt().toString()
                item.grossWeight = grossWeight.roundToInt().toString()
                item.tareWeight = tareWeight.roundToInt().toString()
                item.palletWeight = item.palletWeight?.toDouble()?.roundToInt().toString()

            }

        }
        when (it.fgrnGrades.meins) {
            "MT" -> {
                if(it.fgrnGrades.isRoundOff!!)
                {
                    netWeight = (grossWeight.minus(tareWeight))
                }
                else
                {
                    netWeight = (grossWeight.minus(tareWeight)).div(1000)
                }

            }
            "KG" -> netWeight = grossWeight.minus(tareWeight)
            else -> netWeight = grossWeight.minus(tareWeight)
        }
        if(it.fgrnGrades.isIndexweighmenttype!!)
        {
            netWeight= it.fgrnGrades.netWeight?.toDouble()!!

            var bagItem =VegaCocoaFgrnGradesMatrialWeights()
            bagItem.noOfPallet="0"
            processLot.bagList=listOf(bagItem)


        }
        else
        {
            processLot.bagCount = it.bagItems?.sumBy { it1 -> it1.bagCount.toInt() }.toString()
            for(item in it.bagItems as MutableList<VegaCocoaFgrnGradesMatrialWeights>)
            {
                processLot.bagType = item.bagType
                processLot.huno = item.unitsOfMeasure
                processLot.huwt2 =item.palletAverage
                processLot.nohu2 = item.noOfPallet
            }

            /*processLot.bagType = it.bagItems!![0].bagType
                processLot.huno = it.bagItems!![0].unitsOfMeasure
                processLot.huwt2 = it.bagItems!![0].palletAverage
                processLot.nohu2 = it.bagItems!![0].noOfPallet*/
            processLot.nohu1 = it.bagItems?.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
            processLot.bagList = it.bagItems
            processLot.huno2 = "KG"
            processLot.unitsOfMeasure = it.fgrnGrades.meins
        }

        if (it.fgrnGrades.isRoundOff!!&&!it.fgrnGrades.isIndexweighmenttype!!) {
            //Log.d("netweight")
            processLot.netWeight1 = netWeight.roundToInt().toString()
            processLot.netWeight = netWeight.roundToInt().toString()
            processLot.grossWeight = grossWeight.roundToInt().toString().trim()
            processLot.huwt = bagTareWeight.roundToInt().toString().trim()
        } else {
            processLot.netWeight1 = netWeight.toString()
            processLot.netWeight = netWeight.toString()
            processLot.grossWeight = grossWeight.toString().trim()
            processLot.huwt = bagTareWeight.toString().trim()
        }
        processLot.storageLocationCode = it.fgrnGrades.storageLocationCode
        processLot.unitsOfMeasure1 = "KG"
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
        //processLot.bagMaterialCode = bagItem.bagMaterialCode
        processLot.startTime = it.fgrnGrades.startTime
        processLot.endTime = it.fgrnGrades.endTime
        //processLot.bagList = it.bagItems
        processList.add(processLot)
        poReq.processingLotDtls = processList
    }

    return poReq
}

fun prepareRminCreatePoRequest(
    stageFevor: String, cfgNumber: String,
    materialName: String,
    materialNo: String,
    model: VegaCocoaRminProcessing,
    lotDetails: ArrayList<VegaCocoaRminLots>
): VegaProcessingIndoCreatePoReq {
    val poReq = VegaProcessingIndoCreatePoReq()
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = materialNo
    poReq.materialCode = model.baseMaterialCode
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = model.versionId
    val processList = mutableListOf<VegaProcessingIndoRMINLotDetails>()
    lotDetails.forEach {
        val processLot = VegaProcessingIndoRMINLotDetails()
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
