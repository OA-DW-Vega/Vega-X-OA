package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 4/15/2020.
 */
@Parcelize
data class PileModel(
    var storageLocationCode: String = "",
    var storageLocationName: String = "",
    var classification: String = "",
    var isChecked: Boolean = false
) : Parcelable

data class PilesMaster(
    var piles: List<PileModel>,
    var baleMark: List<String>,
    var cropYear: List<String>
)
