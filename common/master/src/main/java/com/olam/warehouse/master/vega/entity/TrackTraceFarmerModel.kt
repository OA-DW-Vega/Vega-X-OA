package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class TrackTraceFarmerModel(
    @ColumnInfo(index = true)
    @PrimaryKey
    var tmpWbId: String = "",
    var supplier: String? = "",
    var farmer: String? = "",
    var farmerWeight: String? = "",
    var uom: String? = "",
    var id: Int = 0,
    var countryId: String = "",
    var countryName: String = "",
    var farmerId: String = "",
    var farmerName: String = "",
    var farmerGroupName: String = "",
    var farmerGroupId: String = "",
    var productName: String = "",
    var isComplaint: Int = 0,
    var isActive: Boolean = false,
    var createdAt: String = "",
    var createdBy: String = "",
    var updatedAt: String = "",
    var updatedBy: String = "",
    var productId: String = "",
    var cropLimit: Double? = 0.0,
    var uomFromOfis: String? = "",
): Parcelable
