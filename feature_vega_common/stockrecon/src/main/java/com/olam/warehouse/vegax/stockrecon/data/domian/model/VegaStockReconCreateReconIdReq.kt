package com.olam.warehouse.vegax.stockrecon.data.domian.model

data class VegaStockReconCreateReconIdReq(
    var storageLocation: String? = "",
    var plant: String? = "",
    var reconType: String? = "",
    var reportUrl: String? = ""
)
