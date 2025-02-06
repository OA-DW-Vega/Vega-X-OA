package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

@Parcelize
data class VegaApproveAnyLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable



@Parcelize
data class VegaQualityPreParameter(
    var charg: String? = "",
    var appName: String? = "",
    var materialNumber: String? = "",
    var qualityGrade:String="",
    var finalApproval: String? = "",
    var qualityParameters: List<VegaApproveAnyLotQualityParams>? = emptyList()

) : Parcelable


