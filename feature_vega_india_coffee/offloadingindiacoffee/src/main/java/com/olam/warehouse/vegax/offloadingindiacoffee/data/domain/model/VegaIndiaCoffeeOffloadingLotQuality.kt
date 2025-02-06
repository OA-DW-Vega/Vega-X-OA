package com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class VegaIndiaCoffeeOffloadingLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaIndiaCoffeeOffloadingLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaIndiaCoffeeOffloadingLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
