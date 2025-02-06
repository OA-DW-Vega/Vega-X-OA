package com.olam.warehouse.vegax.stockrecon.data.domian.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaStockReconPrintRecipt(
    var id: String? = "",
    var storageLocation: String? = "",
    var warehouse: String? = "",
    var reconType: String? = "",
    var reportUrl: String? = ""
) : Parcelable

