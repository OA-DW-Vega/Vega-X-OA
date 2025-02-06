package com.olam.warehouse.vegax.qualityapprovecameroon.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroonGrnPostData
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.VegaQualityApproveCameroonWeighBridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

const val APPROVE_DATA = "approve_intent_data"
const val APPROVE_PLANT_DATA = "approve_plant_intent_data"
const val APPROVE_WB_DATA = "approve_wb_intent_data"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val FNACCEPT = "X"
const val FNNEGOTIATE = "D"
const val FNREJECT = "D"
const val YES = "Y"
const val KG = "KG"
const val MATERIAL_CODE = "000000"
const val B_PRIMARY_REFR = "B_PRIMARY_REFR"
const val B_SECONDARY_REFR = "B_SECONDARY_REFR"
const val B_PAID_WT = "B_PAID_WT"
const val B_GRNQTY1 = "B_GRNQTY1"
const val B_TOTAL_REFR = "B_TOTAL_REFR"
const val B_GRNPRICE1 = "B_GRNPRICE1"
const val LOBM_UDCODE = "LOBM_UDCODE"
const val DISCOUNTEDWEIGHT1 = "DISCOUNTEDWEIGHT1"
const val B_NETWEIGHT = "B_NETWEIGHT"
const val SOURCE_LOT = "SOURCE_LOT"
const val RECEIVING_PLANT = "RECEIVING_PLANT"
const val SEC_REFRACTION_POST = "Secondary Refraction"
const val PRIM_REFRACTION_POST = "Primary Refraction"
const val USAGE_POST = "Usage Decision"
const val USAGE_DECISION_ACCEPT = "OL-RM    A"
const val AUTO_TRANSFER_T = "T"
const val BEAN_SMOKY = "Bean Smoky"
const val CLASSMENT = "Classment"
const val SMOKY_IR_AB_03 = "IR-AB/PR 003"
const val SMOKY_IR_AB_04 = "IR-AB/PR 004"
const val SMOKY_ABSENCE = "Absence"
const val SMOKY_PRESENCE = "Presence"
const val CLASSMENT_01 = "CLASCOCO 0001"
const val CLASSMENT_02 = "CLASCOCO 0002"
const val CLASSMENT_03 = "CLASCOCO 0003"
const val JSON_DSE_THRESHOLD = "DSE_Threshold" //Threshold Value Stored
var GRN_WEIGHT = "grn_weight"
var isRead = false

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaQualityApproveCameroonWeighBridgeId): VegaQualityApproveCameroonGrnPostData {
    var grnPost = VegaQualityApproveCameroonGrnPostData()
    grnPost.batchNumber = wbDetails.charg
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialNumber
    grnPost.netWeight = wbDetails.grnQty
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice

    grnPost.price = wbDetails.basePrice
    grnPost.supplierCode = wbDetails.supplierCode
    grnPost.unitsOfMeasure = wbDetails.meins
    grnPost.weighBridgeId = wbDetails.wbid
    return grnPost
}
fun prepareData(quality1: List<VegaQualityWithQualitative>): LiveData<List<VegaQualityParamsWithQualitative>> {
    val qualityParamList = arrayListOf<VegaQualityParamsWithQualitative>()
    val qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.quality.position }
            qualitySortedBy.forEach { quality ->
                val qualityParamsQualitative = VegaQualityParamsWithQualitative()
                val qualityParams = VegaQualityParameter()
                qualityParams.wbid = quality.quality.wbid
                qualityParams.wbTempId = quality.quality.wbTempId
                qualityParams.materialCode = quality.quality.materialCode
                qualityParams.descrChar = quality.quality.descrChar
                qualityParams.nameChar = quality.quality.nameChar
                qualityParams.entryObligatory = quality.quality.entryObligatory
                qualityParams.unitText = quality.quality.unitText
                qualityParams.dataType = quality.quality.dataType
                qualityParams.unitsOfMeasure = quality.quality.unitsOfMeasure
                qualityParams.numberDigits = quality.quality.numberDigits
                qualityParams.numberDecimals = quality.quality.numberDecimals
                qualityParams.numValFm = quality.quality.numValFm
                qualityParams.numValTo = quality.quality.numValTo
                qualityParams.currValFm = quality.quality.currValFm
                qualityParams.currValTo = quality.quality.currValTo
                qualityParams.valRelatn = quality.quality.valRelatn
                qualityParams.timeStamp = quality.quality.timeStamp
                qualityParams.preSampling = quality.quality.preSampling
                qualityParams.vegaMandatory = quality.quality.vegaMandatory
                qualityParams.qualityParamLabel = quality.quality.qualityParamLabel
                qualityParams.formulaParam = quality.quality.formulaParam
                qualityParams.qualityParameterValue = quality.quality.qualityParameterValue
                qualityParams.isSyncStatus = quality.quality.isSyncStatus
                qualityParamsQualitative.qualityParameter = qualityParams
                qualityParamsQualitative.qualitative = quality.qualitative
                qualityParamList.add(qualityParamsQualitative)
            }
            qualityList.value = qualityParamList
        }
    }

    return qualityList
}

fun sortByListOfItems(distinctByList: List<VegaQualityParamsWithQualitative>): List<VegaQualityParamsWithQualitative> {
    val sortedList = arrayListOf<VegaQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
}

fun prepareQcWBList(weighBridge: List<VegaQualityWBDetails>): List<VegaQualityApproveCameroonWeighBridge> {
    var list = arrayListOf<VegaQualityApproveCameroonWeighBridge>()
    weighBridge.forEach {
        var item = VegaQualityApproveCameroonWeighBridge()
        item.batchNumber = it.batchNumber
        item.wbid = it.weighBridgeId
        item.charg = it.batchNumber
        item.finalApproval = it.finalApproval
        item.grnNumber = it.grnNumber
        item.werks = it.plant
        item.qualityDetails = it.qualityDetails
        item.materialName = it.materialName
        item.materialNumber = it.materialCode
        item.supplierCode = it.supplierCode
        item.supplierName = it.supplierName
        item.grnQty = it.netWeight
        item.qchar = it.qcStatus
        list.add(item)
    }
    return list
}


