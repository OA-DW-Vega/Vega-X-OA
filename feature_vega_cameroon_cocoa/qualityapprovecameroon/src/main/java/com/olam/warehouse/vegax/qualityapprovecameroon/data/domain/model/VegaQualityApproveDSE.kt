package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model

//Model Class to get response for DSE

data class VegaQualityApproveDSE(
    var responseMessage: String? = "",
    var status: String? = "PENDING",
    var wbDetails: List<VegaQualityApproveCameroonDSEData>? = emptyList()
)
