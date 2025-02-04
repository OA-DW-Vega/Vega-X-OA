package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class PortPileStorageLocationModel(
    var classification: String = "",
    @PrimaryKey
    var storageLocationCode: String = "",
    var storageLocationName: String = "",
    @Ignore
    var bales: List<PortPileBale> = emptyList()
) : Parcelable

data class PortPileRequest(
    val bales: List<PortPileBale>,
    val classification: String,
    val storageLocationCode: String,
    val storageLocationName: String
)

data class PortPileSuccessResponse(
    val success: Boolean = false,
    val message: String = "",
    val MessageV1: String = "",
    val MessageV2: String = ""

)
