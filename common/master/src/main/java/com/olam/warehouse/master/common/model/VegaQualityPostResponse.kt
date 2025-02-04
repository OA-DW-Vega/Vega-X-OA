package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)


data class VegaGhanaMtnrQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)

@Parcelize
data class VegaQualityNigeriaCocoaPostResponse(
    var bcMessage: String? = "",
    var charg: String? = "",
    var currentWbid: String? = "",
    var encodedImageContent: String? = "",
    var errorMessage: String? = "",
    var grnApplicable: Boolean = false,
    var grnFlag: Boolean = false,
    var grnNumber: String? = "",
    var lotQuality: String? = "",
    var message: String? = "",
    var plant: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false,
    var batchDetails: List<VegaBatchDetails?> = emptyList()
) : Parcelable

@Parcelize
data class VegaBatchDetails(
    var atinn: String? = "",
    var atnam: String? = "",
    var atuat: String? = "",
    var atwrt: String? = "",
    var atwtb: String? = "",
    var charNotValid: String? = "",
    var desc: String? = "",
    var matnr: String? = "",
    var werks: String? = "",
    var xdelete: String? = ""
) : Parcelable
