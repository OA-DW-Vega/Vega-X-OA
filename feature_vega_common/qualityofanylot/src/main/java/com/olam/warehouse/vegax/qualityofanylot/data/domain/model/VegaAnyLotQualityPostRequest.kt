package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaAnyLotQualityPostRequest(
    var key: String? = "",
    var transactionNumber: String? = "",
    var batchNumber:String?="",
    var isDummy: Boolean? = true,
    var date: String? = "",
    var materialCode: String? = "",
    var werks: String? = "",
    var materialName: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var uom: String? = "",
    var weight: String? = "",
    var qualityDetails: List<QualityDetails> = emptyList(),
    var isSap:Boolean=false,
    var secretKey: String? = ""
):Parcelable


@Parcelize
data class QualityDetails(
    var descrChar: String? = "",
    var qualityParameterValue: String? = "",
    var nameChar: String? = ""
) : Parcelable
