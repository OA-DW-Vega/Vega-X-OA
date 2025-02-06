package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
@Entity(primaryKeys = ["id"])
@Parcelize
data class VegaPlanRoute(
    var id: Int = 0,
    var departureLocCode: String? = "",
    var departureLocName: String? = "",
    var routeLocationName: String? = "",
    var routeLocCode: String? = "",
    var sourceLocCode: String? = ""
) : Parcelable
