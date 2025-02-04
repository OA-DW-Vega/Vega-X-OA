package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 4/15/2020.
 */
@Parcelize
data class CropYears(
    var cropYear: String = "",
    var isChecked: Boolean = false
) : Parcelable
