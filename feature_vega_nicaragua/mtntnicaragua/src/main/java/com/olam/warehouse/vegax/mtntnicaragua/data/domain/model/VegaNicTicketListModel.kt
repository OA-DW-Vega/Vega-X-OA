package com.olam.warehouse.vegax.mtntnicaragua.data.domain.model

data class VegaNicTicketListModel(
    var materialCode: String = "",
    var lotId: String = "",
    var stockQty: String = "",
    var grnDate: String = "",
    var inventoryQC: List<InventoryQC>,
    var uom: String = "",
    var vendorCode: String = "",
    var isProgress: Boolean = false,
    var vendor: String = ""
)

data class InventoryQC(
    val qcName: String,
    var value: String
)
