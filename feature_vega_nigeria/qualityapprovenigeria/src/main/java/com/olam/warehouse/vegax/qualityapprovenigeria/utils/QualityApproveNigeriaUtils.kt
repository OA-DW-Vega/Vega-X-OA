package com.olam.warehouse.vegax.qualityapprovenigeria.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.work.convertKgToMT
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.VegaNigeriaGrnPostData
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.VegaNigeriaGrnqualityPostData
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.VegaQualityApproveNigeriaGrnPostData
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.VegaQualityApproveNigeriaWeighBridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

const val APPROVE_DATA = "approve_intent_data"
const val APPROVE_PLANT_DATA = "approve_plant_intent_data"
const val APPROVE_WB_DATA = "approve_wb_intent_data"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val FNACCEPT = "X"
const val FNNEGOTIATE = "D"
const val MT = "MT"
const val KG = "KG"
const val FNREJECT = "D"
const val WS = "WS"
const val RECEIVING_PLANT = "RECEIVING_PLANT"
const val DD = "DD"
const val DX = "DX"
const val PR = "PR"
const val PX = "PX"
const val DR = "DR"
const val JUTE_BAG = "JUTE BAG"
const val MATERIAL_CODE = "000000"
const val SUPPLIER_CODE = "000"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(
    wbDetails: VegaQualityApproveCameroonWeighBridge,
    admixtureValue: String,
    bagTypeList: MutableList<VegaPackageMaterial>
): VegaNigeriaGrnPostData {
    var grnPost = VegaNigeriaGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = if (wbDetails.unitsOfMeasure.equals("KG")) convertKgToMT(
        wbDetails.netWeight.toString().trim()
    ) else wbDetails.netWeight.toString().trim()
    grnPost.plant = wbDetails.plantId
    //grnPost . price = if(wbDetails.unitsOfMeasure.equals("KG")) convertKgToMT(wbDetails.unitPrice.toString().trim()) else wbDetails.unitPrice.toString().trim()
    grnPost.price = wbDetails.unitPrice.toString().trim()
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.recPlant = wbDetails.recPlant
    grnPost.recStorageLocation = wbDetails.recStorageLocation
    grnPost.supplierCode = wbDetails.supplierCode
    //grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.unitsOfMeasure = "MT"
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    grnPost.bagCount = wbDetails.bagCount
    grnPost.bagType = wbDetails.bagType
    grnPost.grnModel = wbDetails.grnModel
    grnPost.procurementType = wbDetails.procurementType
    grnPost.finalApproval = "Q"
    grnPost.bagWeight = wbDetails.bagWeight
    var filteredBagTypeList = bagTypeList.filter { it.bagType == wbDetails.bagType }
    if (filteredBagTypeList.size > 0) {
        grnPost.bagMaterialCode = filteredBagTypeList.get(0).bagMaterialCode
    }
    return grnPost
}

fun preparePostGrnData1(
    postData: MutableList<VegaQualityParams>,
    postDataDB: ArrayList<VegaQualityParameter?>
): List<VegaNigeriaGrnqualityPostData?> {
    val grnPost = mutableListOf<VegaNigeriaGrnqualityPostData>()
    postDataDB.forEach {
        val item = VegaNigeriaGrnqualityPostData()
        item.descrChar = it?.descrChar
        postData.forEach { it1 ->
            if (it?.nameChar.equals(it1.sapQCName)) {
                item.nameChar = it1.sapQCName
                item.qualityParameterValue = it1.satNam
            }
        }
        grnPost.add(item)
    }
    return grnPost
}

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

fun preparePostGrnData(wbDetails: VegaQualityApproveNigeriaWeighBridgeId): VegaQualityApproveNigeriaGrnPostData {
    var grnPost = VegaQualityApproveNigeriaGrnPostData()
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
        item.supplierName = it.supplierName
        item.qchar = it.qcStatus
        if (it.unitsOfMeasure.equals("KG",true)){
            item.unitsOfMeasure = MT
            item.grnQty = if (it.netWeight?.isNotEmpty() == true) convertKgToMT(it.netWeight?:"0.0") else "0.0"
            item.grossWeight = if (it.grossWeight?.isNotEmpty() == true) convertKgToMT(it.grossWeight?:"0.0") else "0.0"

        } else {
            item.unitsOfMeasure = it.unitsOfMeasure
            item.grnQty = it.netWeight
            item.grossWeight = it.grossWeight
        }
        list.add(item)
    }
    return list
}
fun isNGCashewEnabled(): Boolean {
    return getCurrentKey().split("_")[1].contains("NG")&& getCurrentKey().split("_")[2].contains("CASH")
}

