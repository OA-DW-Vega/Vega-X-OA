package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["tempId"])
class VegaNicaraguaForwardPODetails(
    var tempId: String = "",
    var cascara: String? = "",
    var certificate: String? = "",
    var createdDate: String? = "",
    var currency: String? = "",
    var docDate: String? = "",
    var exchangeRate: String? = "",
    var grade: String? = "",
    var humedad: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netPrice: String? = "",
    var netWeight: String? = "",
    var pricePerUnit: String? = "",
    var qualityGradeDesc: String? = "",
    var rendimientoBruto: String? = "",
    var unitsOfMeasure: String? = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var syncStatusMsg: String? = "",
    var syncStarted: Int? = 0,
    var poNumber: String? = "",
    var syncStatus: Boolean? = false,
    var poSequenceNumber: String? = "",
    var message: String? = "",
    var taxNumber: String? = "",
    var vendorAddress: String? = "",
    var deliveryDate: String? = "",
    var receiptNetPrice: String? = "",
    @Ignore
    var isProgress: Boolean = false
) : Parcelable
