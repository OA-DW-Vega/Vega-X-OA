package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model

import androidx.annotation.Keep

@Keep
data class VegaGRNDWInventoryModelResponse(
    val data: List<VegaGRNDWLotManualModel>?=ArrayList<VegaGRNDWLotManualModel>(),
    val errorCode: Int,
    val message: String,
    val success: Boolean
)
