package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

data class VegaMaterialStlocDetails(
    var materialCode: String? = "",
    var storageLocationDetail: List<VegaStorageLocationDetail>
)

@Parcelize
@Entity(primaryKeys = ["materialCode", "storageLocationCode"])
data class VegaStorageLocationDetail(
    var plant: String? = "",
    var materialCode: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = ""
) : Parcelable

