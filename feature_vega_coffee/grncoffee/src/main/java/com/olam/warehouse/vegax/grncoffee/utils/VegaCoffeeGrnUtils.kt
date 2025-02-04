package com.olam.warehouse.vegax.grncoffee.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnPostData
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnqualityPostData
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */

const val GRN_FRAG = "grn_fragment"
const val GRN_OFFLINE_FRAG = "grn_offline_fragment"
const val GRN_WB_FRAG = "grn_wb_fragment"
const val GRN_DATA = "grn_data"
const val SPOT = "Spot"
const val FIXED = "Fixed"
const val PO = "po"
const val YEAR = "year"
const val QUALITY_DETAILS = "QualityDetails"
const val PTBF = "Z001"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId): VegaCoffeeGrnPostData {
    val grnPost = VegaCoffeeGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = wbDetails.netWeight
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode
    grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    if (wbDetails.weighMethod == "WB") {
        grnPost.weighBridgeType = "weighBridge"
    } else {
        grnPost.weighBridgeType = "weighScale"
    }
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    return grnPost
}
fun preparePostGrnData1(postData: MutableList<VegaQualityParams>): List<VegaCoffeeGrnqualityPostData?> {
    val grnPost = mutableListOf<VegaCoffeeGrnqualityPostData>()
    postData.forEach {
        val item = VegaCoffeeGrnqualityPostData()
        item.descrChar = it.qualityParameterName
        item.nameChar = it.sapQCName
        item.qualityParameterValue = it.satNam

        grnPost.add(item)
    }
    return grnPost
}

fun prepareWeighBridgeData(receiving: VegaReceiving): VegaGrnWeighBridgeId {
    val qualityWB = VegaGrnWeighBridgeId()
    qualityWB.weighBridgeId = receiving.tmpWbId
    qualityWB.wbTempId = receiving.tmpWbId
    qualityWB.weighBridgeType = receiving.weighBridgeType
    qualityWB.item = receiving.item
    qualityWB.direction = receiving.direction
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.batchNumber = receiving.charg
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (receiving.materialCode?.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = "000".plus(receiving.supplierCode)
    qualityWB.supplierName = receiving.supplierName
    qualityWB.bagCount = receiving.bagCount
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.posnr
    qualityWB.purchaseDocDesc = receiving.purchaseDocDesc
    qualityWB.purchaseDocNum = receiving.purchaseDocNum
    qualityWB.netWeight = receiving.netWeight
    qualityWB.grossWeight = receiving.grossWeight
    qualityWB.unitsOfMeasure = receiving.unitsOfMeasure
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

    return qualityWB
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

