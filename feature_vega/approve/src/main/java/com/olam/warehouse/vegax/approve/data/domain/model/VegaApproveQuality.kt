package com.olam.warehouse.vegax.approve.data.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
@Parcelize
data class VegaApproveQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaApproveQualityParams> = emptyList()
) : Parcelable
