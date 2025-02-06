package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaUploadPrintRequest(
    var key: String? = "",
    var wbid: String? = "",
    var grnNumber: String? = "",
    var imageString:String=""
): Parcelable
