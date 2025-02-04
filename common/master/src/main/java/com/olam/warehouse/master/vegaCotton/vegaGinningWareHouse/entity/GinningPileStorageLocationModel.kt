package com.olam.warehouse.ginning.ui.pile.db.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class GinningPileStorageLocationModel(
    var classification: String = "",
    @PrimaryKey
    var storageLocationCode: String = "",
    var storageLocationName: String = "",
    @Ignore
    var bales: List<PileBale> = emptyList()
) : Parcelable

data class GinningPileRequest(
    val bales: List<PileBale>,
    val classification: String,
    val storageLocationCode: String,
    val storageLocationName: String
)

data class GinningPileSuccessResponse(
    val success: Boolean = false,
    val message: String = "",
    val MessageV1: String = "",
    val MessageV2: String = ""

)
