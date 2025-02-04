package com.olam.warehouse.vegax.bcapprovenigeria.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapprovenigeria.data.domain.model.VegaQualityApproveNigeriaGrnPostData
import com.olam.warehouse.vegax.bcapprovenigeria.data.domain.model.VegaQualityApproveNigeriaWeighBridgeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

const val APPROVE_DATA = "approve_intent_data"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val FNACCEPT = "X"
const val FNREJECT = "D"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

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
