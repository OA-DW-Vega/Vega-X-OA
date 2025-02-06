package com.olam.warehouse.vegax.offloading.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
@Parcelize
data class VegaOffloadingLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaOffloadingLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaOffloadingLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
