package com.olam.warehouse.master.common.data.domain.model

data class VegaProcessingRminPo(
    var autoPoResponse: List<autoPoResponse>? = emptyList()
)

data class autoPoResponse(
    var processingStageId: String? = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

data class messageDetails(
    var type: String? = "",
    var message: String? = "",
    var netWeight: String? = ""
)

