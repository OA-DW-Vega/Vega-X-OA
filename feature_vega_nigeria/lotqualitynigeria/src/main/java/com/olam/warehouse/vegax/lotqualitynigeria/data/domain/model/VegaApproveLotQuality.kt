package com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaApproveLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaApproveLotQualityParams> = emptyList()
) : Parcelable
