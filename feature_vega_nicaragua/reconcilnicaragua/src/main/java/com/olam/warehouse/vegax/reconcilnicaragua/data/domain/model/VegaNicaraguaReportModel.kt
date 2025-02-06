package com.olam.warehouse.vegax.reconcilnicaragua.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNicaraguaReportModel(
    var tempId: String = "",
    var weighBridgeId: String = "",
    var totalPrice: String? = "",
    var erdat: String? = "",
    var supplierName: String? = "",
    var supplierCode: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var grade: String? = "",
    var gradeDesc: String? = "",
    var documentType: String? = "",
    var movementType: String? = "",
    var netWeight: String? = "",
    var netPayment: String? = "",
    var advance: String? = "",
    var itemPos: String? = "",
    var unitOfMeasure: String? = ""
) : Parcelable
