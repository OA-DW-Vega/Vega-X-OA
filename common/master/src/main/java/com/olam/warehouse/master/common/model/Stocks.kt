package com.olam.warehouse.master.common.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stocks(
    @ColumnInfo(index = true)
    var batchNumber: String = "",
    var bkBez: String? = "",
    var bkLas: String? = "",
    var cinsm: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var materialQuality: String? = "",
    var materialText: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = ""
) : Parcelable
