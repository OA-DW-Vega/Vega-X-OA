package com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails

data class VegaSesameQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)
