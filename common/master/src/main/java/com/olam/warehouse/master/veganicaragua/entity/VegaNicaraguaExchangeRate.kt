package com.olam.warehouse.master.veganicaragua.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull


@Entity
data class VegaNicaraguaExchangeRate(

    @PrimaryKey
    @ColumnInfo(index = true)
    var key : String="",
    var currencyCode : String?="",
    var exchangeRate : String?="",
    var currencyValue : String?=""
)
