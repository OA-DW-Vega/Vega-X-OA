package com.olam.warehouse.vegax.processing.utils

import android.text.InputFilter
import android.text.Spanned
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.processing.data.domain.model.ProcessingFgrnLotDetails
import com.olam.warehouse.vegax.processing.data.domain.model.VegaProcessingFgrnPost


/**
 * Created by Baskaran Kannan on 2/1/2020.
 */
//Rmin
const val RMIN = "rmin"
const val FGRN = "fgrn"
const val POSELECTION = "po_selection"
const val GRADESELECTION = "grade_selection"
const val SUMMARY = "summary"
const val LOT_TO_PROCESS = "rmin_lot_to_process"
const val RMINSUMMARY = "rmin_summary"
const val BOM_LIST = "rmin_bom_list"
const val BOM_ITEM = "rmin_bom_item"
const val MATERIAL_NUMBER = "rmin_material_number"
const val MATERIAL_NAME = "rmin_material_name"
const val STAGEFEVOR = "rmin_stage_fevor"
const val CFG_NO = "rmin_cfg_no"


//fgrn
const val PO_DETAILS = "fgrn_po_details"
const val GRADE_LIST = "fgrn_grade_list"
const val PO_DETAILS_LIST = "fgrn_po_details_list"
const val FGRNSUMMARY = "fgrn_summary_details"
const val LOT_DETAILS_LIST = "fgrn_lot_details_list"

const val BATCH_NUMBER = "batch_number"
const val MATERIAL = "material"
const val REGION = "region"
const val KOR = "kor"
const val QUALITY_PARAMS_DATA = "quality_params_data"



fun prepareRminCreatePoRequest(stageFevor: String, cfgNumber: String,
    materialName: String,
    materialNo: String,
    bom: VegaProcessingRminBoms,
    lotDetails: VegaDispatchLots
): VegaProcessingCreatePoReq {
    val poReq = VegaProcessingCreatePoReq()
    poReq.cfgNo = cfgNumber
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = bom.materialCode
    poReq.plant = getPlantDetails()
    poReq.rmin = true
    poReq.versionId = bom.versionId
    val processList = mutableListOf<ProcessingLotDetails>()
    val processLot = ProcessingLotDetails()
    processLot.batchNumber = lotDetails.batchNumber
    processLot.materialCode = lotDetails.materialCode
    processLot.plant = lotDetails.plantId
    processLot.netWeight = lotDetails.weight
    processLot.storageLocationCode = lotDetails.storageLocationCode
    processLot.unitsOfMeasure = lotDetails.unitOfMeasure
    processList.add(processLot)
    poReq.processingLotDtls = processList
    return poReq
}

fun prepareRminCreatePo(
    bom: VegaProcessingRminBoms,
    stageFevor: String,
    materialName: String,
    materialNo: String,
    batchNumber: String
): VegaProcessingCreatePoReq {
    val poReq = VegaProcessingCreatePoReq()
    poReq.cfgNo = bom.cfgno
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.processingStage = stageFevor
    poReq.outputMaterialCode = bom.materialCode
    poReq.materialName = materialName
    poReq.materialCode = materialNo
    poReq.batchNumber = batchNumber
    poReq.date = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
    poReq.rmin = true
    poReq.syncStatusMsg = "Data Cached offline"
    poReq.versionId = bom.versionId
    return poReq
}

fun prepareRminCreatePoToBom(bom: VegaProcessingCreatePoReq): VegaProcessingRminBoms {
    val poReq = VegaProcessingRminBoms()
    poReq.cfgno = bom.cfgNo
    poReq.materialCode = bom.outputMaterialCode.toString()
    poReq.materialName = bom.materialName
    poReq.versionId = bom.versionId
    return poReq
}

fun prepareProcessLots(lotDetails: VegaDispatchLots): ProcessingLotDetails {
    val processLot = ProcessingLotDetails()
    processLot.batchNumber = lotDetails.batchNumber
    processLot.materialCode = lotDetails.materialCode
    processLot.plant = lotDetails.plantId
    processLot.netWeight = lotDetails.weight
    processLot.storageLocationCode = lotDetails.storageLocationCode
    processLot.unitsOfMeasure = lotDetails.unitOfMeasure
    return processLot
}


fun prepareFgrnPostRequest(
    poOrderDetails: VegaFgrnProcessingOrder?,
    lotDetails: List<VegaDispatchLots>
): VegaProcessingFgrnPost {


    val poReq = VegaProcessingFgrnPost()
    poReq.key = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    poReq.outputMaterialCode = poOrderDetails?.materialCode
    poReq.plant = getPlantDetails()
    val processList = mutableListOf<ProcessingFgrnLotDetails>()
    lotDetails.forEach {
        val processLot = ProcessingFgrnLotDetails()
        processLot.batchNumber = it.batchNumber
        processLot.materialCode = it.materialCode
        processLot.plant = it.plantId
        processLot.netWeight = it.weight
        val storeCode = it.storageLocationCode?.split("-")
        processLot.storageLocationCode = storeCode?.get(0)
        processLot.unitsOfMeasure = it.unitOfMeasure
        processLot.bagCount = it.noOfBags
        processLot.confText = PreferenceHelper.get(Constants.USER_NAME, "")
        processLot.deliveryItem = it.deliveryItem
        processLot.menge = it.weight
        processLot.movementType = it.bwart
        processLot.phase = it.phase
        processLot.processOrderNum = it.processOrderNo
        processLot.rsnum = it.rsNum
        processLot.rspos = it.rsPos
        processLot.xchpf = it.xchpf
        processList.add(processLot)
        poReq.processingLotDtls = processList
    }

    return poReq


}


fun prepareLotDetailsToFgrn(
    gradeList: List<VegaFgrnGrades>?, poOrderDetails: VegaFgrnProcessingOrder,
    selectedItemPosition: Int, weight: String, noofBags: String, stroageLocationCode: String, slPostion: Int
): VegaDispatchLots {
    val lotDetails = VegaDispatchLots()
    lotDetails.materialName = gradeList?.get(selectedItemPosition - 1)?.materialName
    lotDetails.weight = weight
    lotDetails.noOfBags = noofBags
    lotDetails.unitOfMeasure = gradeList?.get(selectedItemPosition - 1)?.meins.toString()
    lotDetails.storageLocationCode = stroageLocationCode
    lotDetails.materialCode = gradeList?.get(selectedItemPosition - 1)?.materialCode
    lotDetails.batchNumber = poOrderDetails.rminList!!.get(0).batchNumber.toString()
    lotDetails.plantId = poOrderDetails.plant.toString()
    lotDetails.processOrderNo = poOrderDetails.processOrderNo.toString()
    lotDetails.rsNum = gradeList?.get(selectedItemPosition - 1)?.rsNum
    lotDetails.rsPos = gradeList?.get(selectedItemPosition - 1)?.rsPos
    lotDetails.bwart = gradeList?.get(selectedItemPosition - 1)?.bwart
    lotDetails.phase = gradeList?.get(selectedItemPosition - 1)?.phase
    lotDetails.deliveryItem = gradeList?.get(selectedItemPosition - 1)?.deliveryItem
    lotDetails.xchpf = gradeList?.get(selectedItemPosition - 1)?.xchpf
    lotDetails.slPostion = slPostion

    return lotDetails
}



class DecimalDigitsInputFilter(digitsBeforeZero: Int,private val decimalDigits: Int) : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        var dotPos = -1
        val len = dest.length
        for (i in 0 until len) {
            val c = dest[i]
            if (c == '.' || c == ',') {
                dotPos = i
                break
            }
        }
        if (dotPos >= 0) { // protects against many dots
            if (source == "." || source == ",") {
                return ""
            }
            // if the text is entered before the dot
            if (dend <= dotPos) {
                return null
            }
            if (len - dotPos > decimalDigits) {
                return ""
            }
        }
        return null
    }

}

fun posExtension(tags: MutableSet<String>): Int {
    return if (tags.first().contains("com")) tags.last().toInt() else tags.first().toInt()
}


fun weightToProcess( rminList: List<VegaProcessingList>?,rfgrnList:List<VegaProcessingList>?):Double{
    var rminWeight=0.0
    var fgrnWeight=0.0
    rminWeight = when {
        !rminList.isNullOrEmpty() -> {
            when {rminList[0].netWeight.toString().isEmpty() ->  0.0
                else ->  rminList.sumByDouble { it.netWeight!!.toDouble() } }
        }
        else -> 0.0
    }
    fgrnWeight = when {
        !rfgrnList.isNullOrEmpty() -> {
            when {rfgrnList[0].netWeight.toString().isEmpty() ->  0.0
                else -> rfgrnList.sumByDouble { it.netWeight!!.toDouble() }
            }
        }
        else -> 0.0
    }
    return rminWeight-fgrnWeight
}

