package com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnqualityPostData

data class VegaIndiaCoffeeGrnPostData(
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
    var purchaseDocDesc: String? = "",
    var qcParamValue: String? = "",
    var warehouseRecieptNum: String? = "",
    var billOfLading: String? = "",
    var postingDate: String? = "",
    var pmat2Count: String? = "",
    var pmat2Type: String? = ""
)

data class VegaIndiaCoffeeGrnqualityPostData(
    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)

data class VegaIndiaCoffeeGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaIndiaCoffeeGrnPostData?> = emptyList(),
    var qualityDetails: List<VegaIndiaCoffeeGrnqualityPostData?> = emptyList()
)
