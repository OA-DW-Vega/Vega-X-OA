package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model

import com.olam.warehouse.master.vega.entity.VegaQualityParams


/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
data class VegaQualityApproveCameroon(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()
)
