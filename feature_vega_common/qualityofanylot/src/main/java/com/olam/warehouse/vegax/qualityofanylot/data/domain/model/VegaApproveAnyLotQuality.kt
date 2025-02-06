package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

@Parcelize
data class VegaApproveAnyLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaApproveAnyLotQualityParams> = emptyList()
) : Parcelable

