package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackTraceTransactionIdDetails(
    @PrimaryKey(autoGenerate = true)
    var autoId: Int = 0,
    var dwTransactionId: String? = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var country: String? = "",
    var product: String? = "",
    var totalProductionInMetricTon: String? = "",
    var dateGeoLocationCaptured: String? = "",
    var compliantFlag: Boolean? = false
)
