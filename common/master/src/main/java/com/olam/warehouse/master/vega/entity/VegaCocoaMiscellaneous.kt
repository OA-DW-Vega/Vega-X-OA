package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaCocoaMiscellaneous(
    var id: Int = 0,
    @ColumnInfo(name = "roleKey")
    var key: String = "",
    var plant: String = "",
    var json: String? = ""
) : Parcelable
