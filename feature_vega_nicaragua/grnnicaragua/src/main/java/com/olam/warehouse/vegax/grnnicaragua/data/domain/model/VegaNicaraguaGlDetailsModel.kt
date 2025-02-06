package com.olam.warehouse.vegax.grnnicaragua.data.domain.model

data class VegaNicaraguaGlDetailsModel(
    val GL_DETAILS: List<TollingDetails>
)

data class TollingDetails(
    var COST_CENTER: String? = "",
    var GL_ACCOUNT: String? = ""
)
