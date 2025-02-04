package com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails

data class VegaNigeriaSesameQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList()
)

data class VegaNigeriaSesameQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var encodedImageContent: String? = "",
    var success: Boolean = false
)
