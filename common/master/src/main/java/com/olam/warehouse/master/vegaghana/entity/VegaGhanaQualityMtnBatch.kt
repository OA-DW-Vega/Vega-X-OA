package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["mtnNumber", "posnr"])
data class VegaGhanaQualityMtnBatch(
    var mtnNumber: String = "",
    var posnr: String = "",
    var batchNumber: String? = "",
    var deliveryQty: String? = "",
    var deliveryUOM: String? = "",
    var stockQty: String? = "",
    var stockUOM: String? = "",
    var storageLocationCode: String? = "",
    var werks: String? = ""
) : Parcelable
