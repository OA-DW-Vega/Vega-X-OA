package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaQualityApproveIndiaCoffeeGrnPostData(
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
    var batchNumber: String? = ""
)

data class VegaQualityApproveIndiaCoffeeGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaQualityApproveIndiaCoffeeGrnPostData?> = emptyList()
)
