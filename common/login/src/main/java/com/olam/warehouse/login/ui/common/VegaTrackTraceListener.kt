package com.olam.warehouse.login.ui.common

import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails

interface VegaTrackTraceListener {
    fun isVendor(flag:Boolean)

    fun isComplaint(status: Int)
    fun updateSourceLotDetails(sourceLotDetails: TrackTraceSourceLotDetails)

    fun updateTransactionIdDetails(transactionIdDetails: TrackTraceTransactionIdDetails)

    fun updateFarmerDetails(farmerListDetails: ArrayList<TrackTraceFarmerModel>)
}
