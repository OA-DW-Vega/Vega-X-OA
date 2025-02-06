package com.olam.warehouse.vegax.grnindiacoffee.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGrnPostData
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGrnqualityPostData
import kotlin.random.Random


const val GRN_FRAG = "grn_fragment"
const val GRN_OFFLINE_FRAG = "grn_offline_fragment"
const val GRN_WB_FRAG = "grn_wb_fragment"
const val GRN_DATA = "grn_data"
const val QUALITY_DATA = "quality_data"
const val SPOT = "Spot"
const val FIXED = "Z001"
const val GRN_QUALITY_DETAILS = "grn_quality_details"
const val CROP_GUNNY_BAG = "CROP GUNNY BAG"
const val PO = "po"
const val YEAR = "year"
const val PTBF = "Z002"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId, admixtureValue: String,postedDate: String): VegaIndiaCoffeeGrnPostData {
    var grnPost = VegaIndiaCoffeeGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = wbDetails.netWeight
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode
    grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    grnPost.purchaseDocDesc = wbDetails.purchaseDocDesc
    grnPost.qcParamValue = admixtureValue
    grnPost.warehouseRecieptNum=wbDetails.warehouseRecieptNum
    grnPost.billOfLading=wbDetails.billOfLading
    grnPost.postingDate=postedDate
    grnPost.pmat2Count=wbDetails.pmat2Count
    grnPost.pmat2Type=wbDetails.pmat2Type
    return grnPost
}

fun preparePostGrnData1(postData: MutableList<VegaQualityParams>): List<VegaIndiaCoffeeGrnqualityPostData?> {
    val grnPost = mutableListOf<VegaIndiaCoffeeGrnqualityPostData>()
    postData.forEach {
        val item = VegaIndiaCoffeeGrnqualityPostData()
        //item.descrChar = it.qualityParameterName
        item.descrChar = ""
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

