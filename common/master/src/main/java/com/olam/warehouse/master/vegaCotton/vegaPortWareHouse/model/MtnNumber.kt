package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MtnNumber (
    var baleID: String? = "",
    var isScaned: Boolean? = false
) : Parcelable
