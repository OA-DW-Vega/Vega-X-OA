package com.olam.warehouse.vegax.processingnigeria.data.domain.model

import com.olam.warehouse.master.common.data.domain.model.messageDetails

data class VegaNigeriaProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var encodedImageContent: String? = "",
    var messages: List<messageDetails>? = emptyList()
)
