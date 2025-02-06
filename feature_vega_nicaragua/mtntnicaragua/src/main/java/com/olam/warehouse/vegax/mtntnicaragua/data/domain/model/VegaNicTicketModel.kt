package com.olam.warehouse.vegax.mtntnicaragua.data.domain.model

data class VegaNicTicketModel(
    var weighBridgeId: String = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var netWeight: String = "",
    var erdat: String = "",
    var bagCount: String = "",
    var grossWeight: String = "",
    var unitsOfMeasure: String = "",
    var isProgress: Boolean = false,
    var transportVendorCode: String = "",
    var grade: String = "",
    var certificate: String = "",
    var ticket: String = ""
)



