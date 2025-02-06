package com.olam.warehouse.vegax.processing.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaProcessingQualityDetails(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaProcessingQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaProcessingQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
