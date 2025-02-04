package com.olam.warehouse.vegax.processingcoffee.data.domain.model

import com.olam.warehouse.master.common.data.domain.model.messageDetails

data class VegaCoffeeProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var encodedImageContent: String? = "",
    var messages: List<messageDetails>? = emptyList()
)
