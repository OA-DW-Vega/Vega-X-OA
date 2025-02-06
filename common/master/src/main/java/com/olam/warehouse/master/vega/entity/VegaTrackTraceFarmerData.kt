package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity()
@Parcelize
data class VegaTrackTraceFarmerData(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int = 0,
    var id: Int = 0,
    var countryId: String = "",
    var farmerId: String = "",
    var farmerName: String = "",
    var farmerGroupName: String = "",
    var farmerGroupId: String = "",
    var isComplaint: Int = 0,
    var active: Boolean = false,
    var productId: String = "",
    var createdAt: String = "",
    var createdBy: String = "",
    var updatedAt: String = "",
    var updatedBy: String = "",
    var cropLimit: Double? = 0.0,
    var uom: String? = ""
) : Parcelable

