package com.olam.warehouse.vegax.grnindo.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPostData
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */


const val GRN_FRAG = "grn_fragment"
const val GRN_OFFLINE_FRAG = "grn_offline_fragment"
//const val GRN_WB_FRAG = "grn_wb_fragment"
const val GRN_DATA = "grn_data"
const val SPOT = "Spot"
const val FIXED = "Fixed"
const val PROCURE = "PROCURE"
const val QUALITY_DETAILS = "QualityDetails"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId): VegaEcuadorGrnPostData {
    val grnPost = VegaEcuadorGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = wbDetails.netWeight
    grnPost.plant = wbDetails.plantId
    grnPost.price = wbDetails.unitPrice
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode?.replace("000000", "000")
    grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.currency = "IDR"
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    grnPost.purchaseDocDesc = wbDetails.purchaseDocDesc
    grnPost.qualityDetails = wbDetails.qualityDetails
    grnPost.billOfLading= wbDetails.billOfLading
    grnPost.reference=wbDetails.reference
    grnPost.deliveryNote=wbDetails.deliveryNote
    grnPost.headerText=wbDetails.headerText
    return grnPost
}

/*fun prepareWeighBridgeData(receiving: VegaReceiving): VegaGrnWeighBridgeId {
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
}*/

fun prepareQualityWeighBridgeData(receiving: VegaQualityWBDetails): VegaGrnWeighBridgeId {
    val qualityWB = VegaGrnWeighBridgeId()
    qualityWB.weighBridgeId = receiving.weighBridgeId
    qualityWB.wbTempId = receiving.wbTempId
    qualityWB.weighBridgeType = receiving.weighBridgeType
    qualityWB.item = if (receiving.item?.length == 1) "0000".plus(receiving.item) else receiving.item
    qualityWB.qcStatus = "X"
    qualityWB.direction = receiving.direction
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.batchNumber = receiving.batchNumber
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (receiving.materialCode?.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = "000".plus(receiving.supplierCode)
    qualityWB.supplierName = receiving.supplierName
    qualityWB.storageLocationCode = receiving.storageLocationCode
    qualityWB.bagCount = receiving.bagCount
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.purchaseDocDesc = receiving.purchaseDocDesc
    qualityWB.purchaseDocNum = receiving.purchaseDocNum
    qualityWB.netWeight = receiving.netWeight.toString()
    qualityWB.grossWeight = receiving.grossWeight
    qualityWB.unitsOfMeasure = receiving.unitsOfMeasure
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

    return qualityWB
}
