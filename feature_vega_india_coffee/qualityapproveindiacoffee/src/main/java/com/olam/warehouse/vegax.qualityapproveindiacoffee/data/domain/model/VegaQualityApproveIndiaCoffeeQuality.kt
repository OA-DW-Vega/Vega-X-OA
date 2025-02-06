package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model

import com.olam.warehouse.master.vega.entity.VegaQualityParams



data class VegaQualityApproveIndiaCoffee(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()
)
