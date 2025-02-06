package com.olam.warehouse.master.vegacameroon.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity
@Parcelize
data class VegaCameroonShippingLine(
    @ColumnInfo(index = true)
    @PrimaryKey
    var id: Int = 0,
    var shippingLineNm:String = "",
    var shippingLineDesc:String = ""
) : Parcelable
