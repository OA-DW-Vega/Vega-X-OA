package com.olam.warehouse.master.vega.model

data class TrackTraceScannerSourceLotDetails(
    var sourceLotId: String? = "",
    var complianceIndicator: Boolean? = false,
    var vendorId: String? = "",
    var productCode: String? = "",
)
