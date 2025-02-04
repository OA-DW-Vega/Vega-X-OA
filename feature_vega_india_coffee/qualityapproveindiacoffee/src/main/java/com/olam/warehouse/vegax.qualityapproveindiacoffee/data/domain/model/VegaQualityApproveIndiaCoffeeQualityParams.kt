package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize



@Parcelize
data class VegaQualityApproveIndiaCoffeeQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
