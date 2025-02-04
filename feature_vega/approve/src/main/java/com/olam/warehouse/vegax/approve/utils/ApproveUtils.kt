package com.olam.warehouse.vegax.approve.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approve.data.domain.model.VegaApproveWeighBridgeId
import com.olam.warehouse.vegax.approve.data.domain.model.VegaGrnPostData

/**
 * Created by Baskaran Kannan on 1/21/2020.
 */

const val APPROVE_DATA = "approve_intent_data"
const val APPROVE_QUALITY_DATA = "approve_quality_data"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(wbDetails: VegaApproveWeighBridgeId): VegaGrnPostData {
    var grnPost = VegaGrnPostData()
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
    return grnPost
}
