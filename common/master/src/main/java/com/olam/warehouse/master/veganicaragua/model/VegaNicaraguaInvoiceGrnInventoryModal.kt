package com.olam.warehouse.master.veganicaragua.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class VegaNicaraguaInvoiceGrnInventoryModal (
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var tareWeight: String? = "",
    var netPrice: String? = "",
    var humidity: String? = "",
    var bagCount: String? = "",
    var qualityGrade: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var exportable: String? = "",
    var certification: String? = ""

):Parcelable
