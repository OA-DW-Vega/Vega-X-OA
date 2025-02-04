package com.olam.warehouse.ginning.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Grade(
    val grade: String? = "",
    var isSelect: Boolean? = false
) : Parcelable