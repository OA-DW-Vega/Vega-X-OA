package com.olam.warehouse.vegax.bcapprovecameroon.data.domain.model

import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 2/7/2020.
 */
data class VegaQualityApproveCameroonGrnPostData(
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

data class VegaQualityApproveCameroonGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaQualityApproveCameroonGrnPostData?> = emptyList()
)
