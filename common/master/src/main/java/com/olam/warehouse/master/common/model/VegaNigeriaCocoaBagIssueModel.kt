package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNigeriaCocoaBagIssue(
    var materialCode: String = "",
    var materialName: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var currentBalance: String? = "",
    var bagIssued: String? = "",
    var gatePassNum: String? = "",
    var screenType: String? = "",
    var documentNumber: String? = ""
): Parcelable
