package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaQualityPreParameter(
    var charg: String? = "",
    var appName: String? = "",
    var materialNumber: String? = "",
    var qualityGrade:String="",
    var finalApproval: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()

) : Parcelable

@Parcelize
data class VegaQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = "",
    var sapQCNameDesc: String? = "",
) : Parcelable

