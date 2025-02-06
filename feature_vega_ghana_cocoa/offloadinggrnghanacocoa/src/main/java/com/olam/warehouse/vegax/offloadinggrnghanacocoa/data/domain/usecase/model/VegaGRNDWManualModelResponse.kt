package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model

data class VegaGRNDWManualModelResponse(
    val `data`: VegaGRNDWLotManualModel?=null,
    val errorCode: Int,
    val message: String,
    val success: Boolean
)
