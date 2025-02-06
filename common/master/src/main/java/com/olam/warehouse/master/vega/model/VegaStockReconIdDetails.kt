package com.olam.warehouse.master.vega.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaStockReconIdDetails(
    var id: String = "",
    var storageLocation: String = "",
    var plant: String? = "",
    var warehouse: String? = "",
    var reconType: String = "",
    var totalNoOfLots: String? = "",
    var reportUrl: String? = "",
    var status: String? = "",
    var createdAt: String? = ""
) : Parcelable
