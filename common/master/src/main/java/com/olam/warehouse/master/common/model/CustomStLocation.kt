package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */
@Parcelize
data class CustomStLocation(
    var plant: String? = "",
    var procureLocationCode: String? = "",
    var procureLocationName: String? = "",
    var storageLocationType: String? = "",
    var binDetails: List<BinDetails>? = emptyList()
):Parcelable

@Parcelize
data class BinDetails(
    var binLocationCode: String? = "",
    var procureLocationCode: String? = "",
    var binLocationName: String? = ""
): Parcelable
