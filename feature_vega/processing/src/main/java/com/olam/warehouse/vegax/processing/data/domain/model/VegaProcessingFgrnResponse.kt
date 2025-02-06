package com.olam.warehouse.vegax.processing.data.domain.model

import com.olam.warehouse.master.common.data.domain.model.messageDetails

data class VegaProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

