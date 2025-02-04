package com.olam.warehouse.vegax.grnnigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails

data class VegaNigeriaGrnPostData(
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
    var pmat2Type: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "",
    var bagMaterialCode: String? = "",
    var finalApproval: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
)

data class VegaNigeriaGrnqualityPostData(
    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)

data class VegaCameroonQcPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList()
)

data class VegaNigeriaGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaNigeriaGrnPostData?> = emptyList(),
    var qualityDetails: List<VegaNigeriaGrnqualityPostData?> = emptyList(),
    val userName: String,
    val grntNumber: String
)

data class VegaNigeriaCocoaQualityApprovePostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var encodedImageContent: String? = "",
    var success: Boolean = false
)
