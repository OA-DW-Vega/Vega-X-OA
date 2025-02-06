package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(indices = [Index(value = ["baleID", "deliveryNumber"], unique = true)])
@Parcelize
data class Bale(
    @PrimaryKey
    var baleID: String = "",
    var otNumber: String? = "",
    var warehouseLotID: String? = "",
    var warehouseLocation: String? = "",
    var lotNumber: String? = "",
    var containerNumber: String = "",
    var deliveryNumber: String = "",
    var grade: String? = "",
    var grossWeight: Double? = 0.0,
    var netWeight: Double? = 0.0,
    var createdTS: String? = "",
    var shift: String? = "",
    var userID: String? = "",
    var userName: String? = "",
    var isLastBaleOfShift: Boolean? = false,
    var isUsed: Boolean? = false,
    var isHold: Boolean? = false,
    var storageLocationId: Int = 0,
    var storageLocationTo: Int = 0

) : Parcelable
