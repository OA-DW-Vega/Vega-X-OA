package com.olam.warehouse.vegax.stockrecon.data.domian.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaReconReportReconList(
    var dateRange: String? = "",
    var plant: String? = "",
    var totalNoOfRecons: String? = "",
    var totalNoOfLots: String? = "",
    var totalSystemWeight: String? = "",
    var totalAuditWeight: String? = "",
    var totalGainLoss: String? = "",
    var unitOfMeasure: String? = "",
    var stockReconList: List<StockReconList> = emptyList()
) : Parcelable

@Parcelize
data class StockReconList(
    var id: String? = "",
    var reconType: String? = "",
    var totalNoOfLots: String? = "",
    var totalGainLoss: String? = "",
    var totalSystemWeight: String? = "",
    var totalAuditWeight: String? = "",
    var unitOfMeasure: String? = "",
    var createdAt: String? = "",
    var reportUrl: String? = ""
) : Parcelable
