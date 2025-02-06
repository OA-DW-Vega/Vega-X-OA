package com.olam.warehouse.odquality.data.domain.model

data class DOQualitySavedResponse(
    var batchNumber: String = "",
    var materialNumber: String = "",
    var plantId: String = "",
    var qualityParameters: List<QualityParameter>? = null,
    var qualityParams: QualityParams? = null
)

data class QualityParameter(
    var descrChar: String? = null,
    var nameChar: String? = null,
    var qualityParameterDataType: String? = null,
    var qualityParameterValue: String? = null
)

class QualityParams
