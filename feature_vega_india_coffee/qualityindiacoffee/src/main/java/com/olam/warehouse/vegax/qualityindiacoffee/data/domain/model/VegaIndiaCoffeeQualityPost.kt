package com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails

data class VegaIndiaCoffeeQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList()
)

data class VegaIndiaCoffeeQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)
data class IndiaCoffeeFilterList(val value: String, var isSelected: Boolean = false)
