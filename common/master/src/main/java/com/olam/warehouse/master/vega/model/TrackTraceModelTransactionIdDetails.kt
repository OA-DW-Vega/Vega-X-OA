package com.olam.warehouse.master.vega.model

import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails

data class TrackTraceModelTransactionIdDetails(
    var dwTransactionId: String = "",
    var vendorDetails: List<TrackTraceTransactionIdDetails> = emptyList()
)
