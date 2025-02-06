package com.olam.warehouse.master.veganicaragua.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNicaraguaForwardPoPost(

    var cascara: String? = "",
    var certificate: String? = "",
    var createdDate: String? = "",
    var currency: String? = "",
    var docDate: String? = "",
    var exchangeRate: String? = "",
    var grade: String? = "",
    var humedad: String? = "",
    var key: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netPrice: String? = "",
    var netWeight: String? = "",
    var plant: Plant? = null,
    var priceDetails: List<VegaNicaraguaGrnPriceDetails>? = null,
    var pricePerUnit: String? = "",
    var qualityGradeDesc: String? = "",
    var rendimientoBruto: String? = "",
    var unitsOfMeasure: String? = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var poSequenceNumber: String? = "",
    var poNumber: String? = "",
    var message: String? = "",
    var taxNumber: String? = "",
    var vendorAddress: String? = "",
    var deliveryDate: String? = "",
    var receiptNetPrice: String? = ""
) : Parcelable

