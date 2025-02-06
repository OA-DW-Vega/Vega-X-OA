package com.olam.warehouse.vegax.qualityapproveindiacoffee.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.VegaQualityApproveIndiaCoffeeGrnPostData
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.VegaQualityApproveIndiaCoffeeWeighBridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val APPROVE_DATA = "approve_intent_data"
const val APPROVE_PLANT_DATA = "approve_plant_intent_data"
const val APPROVE_WB_DATA = "approve_wb_intent_data"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val FNACCEPT = "X"
const val FNNEGOTIATE = "D"
const val FNREJECT = "D"
const val QUALITY = "QUALITY"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaQualityApproveIndiaCoffeeWeighBridgeId): VegaQualityApproveIndiaCoffeeGrnPostData {
    var grnPost = VegaQualityApproveIndiaCoffeeGrnPostData()
    grnPost.batchNumber = wbDetails.charg
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialNumber
    grnPost.netWeight = wbDetails.grnQty
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
//    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode
    grnPost.unitsOfMeasure = wbDetails.meins
//    grnPost.weighBridgeType = wbDetails.wb
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
        item.supplierName = it.supplierCode
        item.grnQty = it.netWeight
        list.add(item)
    }
    return list
}


