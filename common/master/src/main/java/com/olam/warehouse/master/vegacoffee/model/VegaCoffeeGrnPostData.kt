package com.olam.warehouse.master.vegacoffee.model

import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
data class VegaCoffeeGrnPostData(
    var batchNumber: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var storageLocationCode: String? = "",
    var price: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",
    var materialCode: String? = "",
    var item: String? = "",
    var purchaseDocNum: String? = "",
    var finalApproval: String? = "Q"
)
data class VegaCoffeeGrnqualityPostData(
    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)

data class VegaCoffeeGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaCoffeeGrnPostData?> = emptyList(),
    var qualityDetails: List<VegaCoffeeGrnqualityPostData?> = emptyList()
)
