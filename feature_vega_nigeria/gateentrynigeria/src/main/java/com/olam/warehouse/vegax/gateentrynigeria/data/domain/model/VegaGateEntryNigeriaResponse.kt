package com.olam.warehouse.vegax.gateentrynigeria.data.domain.model

/**
 * Created by Baskaran Kannan on 3/11/2020.
 */

data class VegaGateEntryNigeriaResponse(val wbId: String? = "", val txnId: String? = "")


data class VegaGateEntryResponse(
    var errorCode: String? = "",
    var message: String? = "",
    var success: Boolean = false,
    val data: List<VegaNigeriaGateEntryPostData> = emptyList()
)

data class VegaNigeriaGateEntryPostData(
    var currency: String? = "",
    var grnNumber: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var netWeight: String? = "",
    var plantId: String? = "",
    var price: String? = "",
    var procureType: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var storageLocationCode: String? = "",
    var supplierCode: String? = "",
    var unitsOfMeasure: String? = "",
    var weighBridgeId: String? = "",
    var year: String? = ""
)
