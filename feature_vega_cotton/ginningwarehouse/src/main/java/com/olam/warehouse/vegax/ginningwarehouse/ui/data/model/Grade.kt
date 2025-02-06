package com.olam.warehouse.vegax.ginningwarehouse.ui.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Grade(
    val grade: String? = "",
    var isSelect: Boolean? = false
) : Parcelable