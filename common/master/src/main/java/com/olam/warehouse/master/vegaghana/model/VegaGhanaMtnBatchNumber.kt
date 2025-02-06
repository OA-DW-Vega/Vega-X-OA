package com.olam.warehouse.master.vegaghana.model

data class VegaGhanaMtnBatchNumber(
    var mtnBatchDetails: VegaGhanaMtnBatchDetails,
    var mtnNumber: String,
    var posnr: String
)

data class VegaGhanaMtnBatchDetails(
    var batchNumber: String,
    var deliveryQty: String,
    var deliveryUOM: String,
    var stockQty: String,
    var stockUOM: String,
    var storageLocationCode: String,
    var werks: String

)
