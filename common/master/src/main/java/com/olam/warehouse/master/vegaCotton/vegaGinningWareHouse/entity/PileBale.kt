package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(indices = [Index(value = ["baleID", "storageLocationTo"], unique = true)])
@Parcelize
data class PileBale(
    @PrimaryKey
    var baleID: String = "",
    var otNumber: String? = "",
    var warehouseLotID: String? = "",
    var warehouseLocation: String? = "",
    var classMatnr: String? = "",
    var grade: String? = "",
    var grossWeight: String? = "",
    var netWeight: String? = "",
    var createdTS: String? = "",
    var shift: String? = "",
    var userID: String? = "",
    var userName: String? = "",
    var isUsed: Boolean? = false,
    var storageLocationId: String = "",
    var storageLocationTo: String = ""
) : Parcelable
