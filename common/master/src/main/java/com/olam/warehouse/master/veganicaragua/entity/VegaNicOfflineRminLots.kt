package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(primaryKeys = ["batchNumber", "rminTempId"])
data class VegaNicOfflineRminLots(
    var batchNumber: String = "",
    var rminTempId: String = "",
    var poNo: String? = "",
    var storageLocationCode: String? = "",
    var materialName: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
    var lotId: String = ""

) : Parcelable

@Parcelize
@Entity(primaryKeys = ["materialCode", "rminTempId"])
data class VegaNicOfflineRminItems(
    var materialCode: String = "",
    var rminTempId: String = "",
    var materialName: String? = "",
    var weightToProcess: String? = ""

) : Parcelable
