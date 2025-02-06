package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["batchNumber", "rminTempId"])
data class VegaGhanaOfflineRminLots(
    var batchNumber: String = "",
    var rminTempId: String = "",
    var storageLocationCode: String? = "",
    var materialName: String? = "",
    var weight: String? = "",
    var editedWeight: String? = ""

): Parcelable

@Parcelize
@Entity(primaryKeys = ["materialCode", "rminTempId"])
data class VegaGhanaOfflineRminItems(
    var materialCode: String = "",
    var rminTempId: String = "",
    var materialName: String? = "",
    var weightToProcess: String? = ""

): Parcelable
