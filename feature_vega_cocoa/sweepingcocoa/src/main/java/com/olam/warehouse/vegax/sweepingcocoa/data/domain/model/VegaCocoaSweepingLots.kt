package com.olam.warehouse.vegax.sweepingcocoa.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 5/28/2020.
 */
@Parcelize
data class VegaCocoaSweepingLots(
    var batchNumber: String = "",
    var bkBez: String? = "",
    var bkLas: String? = "",
    var cinsm: String? = "",
    var msg: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = ""

) : Parcelable

@Parcelize
data class VegaSweepingSummary(
    var lot: VegaCocoaSweepingLots,
    var palletWeight: String,
    var palletCount: String,
    var avgPalletCount: String,
    var grossWeight: String,
    var netWeight: String,
    var tareWeight: String,
    var noOfBags: String
) : Parcelable

data class VegaSweepingPost(
    var key: String,
    var lotDetails: VegaCocoaSweepingLots = VegaCocoaSweepingLots()
)

data class VegaSweepingResponse(
    var batchNumber: String? = "",
    var msg: String? = ""
)
