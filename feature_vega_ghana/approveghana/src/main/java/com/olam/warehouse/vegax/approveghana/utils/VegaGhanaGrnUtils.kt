package com.olam.warehouse.vegax.approveghana.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.GhanaGRNQualityDetails
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGhanaGrnPostData
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */

const val GRN_FRAG = "grn_fragment"
const val GRN_OFFLINE_FRAG = "grn_offline_fragment"
const val GRN_WB_FRAG = "grn_wb_fragment"
const val GRN_DATA = "grn_data"
const val GRN_ADMIXTURE_VALUE = "admixture_value"
const val GRN_UNIT_HEAD = "unithead_value"
const val QUALITY_DATA = "quality_data"
const val SPOT = "Spot"
const val FIXED = "Fixed"
const val GRN_QUALITY_DETAILS = "grn_quality_details"
const val FNREJECT = "D"
const val FNQUALITY = "Q"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaGrnWeighBridgeId, admixtureValue: String, list: ArrayList<GhanaGRNQualityDetails>,unitHead:String): VegaGhanaGrnPostData {
    var grnPost = VegaGhanaGrnPostData()
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
    grnPost.qcParamValue = admixtureValue
    grnPost.unitHead = unitHead
   // grnPost.qualityDetails = list
    grnPost.finalApproval = wbDetails.finalApproval
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

