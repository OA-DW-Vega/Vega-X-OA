package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Grade(
    var grade: String? = "",
    var isSelect: Boolean? = false
) : Parcelable
