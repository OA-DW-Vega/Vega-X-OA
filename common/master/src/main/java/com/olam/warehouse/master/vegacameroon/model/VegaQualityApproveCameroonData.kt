package com.olam.warehouse.master.vegacameroon.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaQualityApproveCameroonPostData(
    var weighBridgeId: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var supplierCode: String? = "",
    var batchNumber: String? = "",
    var waers: String? = "",
    var plant: String? = "",
    var discount: String? = "",
    var priceCharacter: String? = "",
    var qchar: String? = "",
    var paidWeight: String? = "",
    var grnQty: String? = "",
    var finalApproval: String? = "",
    var autoTransfer: String? = "",
    var receivingStorageLoc: String? = "",
    var sendingStorageLoc: String? = "",
    var uom: String? = "",
    var qualityDetails: List<QualityDetails> = emptyList()
) : Parcelable

@Parcelize
data class QualityDetails(
    var descrChar: String? = "",
    var qualityParameterValue: String? = "",
    var nameChar: String? = ""
) : Parcelable
