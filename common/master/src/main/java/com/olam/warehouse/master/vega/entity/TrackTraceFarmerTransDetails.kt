package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class TrackTraceFarmerTransDetails(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var tmpWbId: String? = "",
    var countryId: String? = "",
    var productId: String? = "",
    var countryName: String? = "",
    var createdAt: String? = "",
    var createdBy: String? = "",
    var netWeight: String? = "",
    var uom: String? = "",
    var updatedAt: String? = "",
    var updatedBy: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = ""
): Parcelable
