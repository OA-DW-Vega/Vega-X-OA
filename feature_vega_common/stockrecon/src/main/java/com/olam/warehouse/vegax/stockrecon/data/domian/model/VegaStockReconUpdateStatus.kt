package com.olam.warehouse.vegax.stockrecon.data.domian.model

data class VegaStockReconUpdateStatus(
    var id: String? = "",
    var storageLocation: String? = "",
    var plant: String? = "",
    var warehouse: String? = "",
    var reconType: String? = "",
    var totalNoOfLots: String? = "",
    var reportUrl: String? = "",
    var status: String? = "",
    var createdAt: String? = ""
)
