package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(indices = [Index(value = ["baleId", "containerNumber", "otNumber"], unique = true)])
@Parcelize
data class PortBale(
    @PrimaryKey
    var baleId: String = "",
    var otNumber: String? = "",
    var containerNumber: String = "",
    var grade: String? = "",
    var grossWeight: String? = "",
    var netWeight: Double? = 0.0,
    var delyNetQty: Double? = 0.0,
    var createdTS: String? = "",
    @Ignore
    var typeofBale: String? = "",
    @Ignore
    var typeOfBale: String? = "",
    @Ignore
    var fromSloc: String? = "",
    @Ignore
    var toSloc: String? = "",
    var baleMark: String? = ""
) : Parcelable


