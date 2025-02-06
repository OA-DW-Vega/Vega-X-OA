package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Keep
@Parcelize
data class VegaGRNDWLotManualModel(
    val certification: List<String>?=ArrayList<String>(),
    val lotId: String?="",
    val netWeight: String?="",
    val noOfBags: String?="",
    val productGrade: String?="",
    val productName: String?="",
    val sentDate: String?="",
    val status: String?="",
    val unitOfMeasurement: String?="",
    val vendorName: String?="",
    val vendorSapCode: String?="",
    var isAdded:Boolean =false,
    var isNormalAdd:Boolean=false
): Parcelable

