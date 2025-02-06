package com.olam.warehouse.master.vega.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackTraceSourceLotDetails(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int = 0,
    var id: String? = "",
    var plant: String? = "",
    var countryId: String? = "",
    var productCode: String? = "",
    var sourceLotId: String? = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var isEudrComplaintFlag: Boolean? = false,
    var createdAt: String? = "",
    var updatedAt: String? = "",
)
