package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class VegaNicaraguaReconcilCashMovement(
    @PrimaryKey
    @ColumnInfo(index = true)
    var edate: String = "",
    var openingCash: String? = "",
    var remittances: String? = "",
    var urgentAdvance: String? = "",
    var ptbfAdvance: String? = "",
    var cashBalance: String? = "",
    var thousands: Int? = 0,
    var fiveHundred: Int? = 0,
    var twoHundred: Int? = 0,
    var hundred: Int? = 0,
    var fifty: Int? = 0,
    var twenty: Int? = 0,
    var ten: Int? = 0,
    var five: Int? = 0,
    var two: Int? = 0,
    var one: Int? = 0,
    var half: Int? = 0
) : Parcelable

/*data class VegaNicaraguareconcilDenomination(
    //var thousands
)*/
