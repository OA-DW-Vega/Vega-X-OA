package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class VegaEcuadorBcApproveWBDetails (
    var batchNumber: String? = "",
    var discount: String? = "",
    var discountWeight: String? = "",
    var grn: String? = "",
    var grnQty: String? = "",
    var grnNumber: String? = "",
    var grnType: String? = "",
    var item: String? = "",
    var materialName: String? = "",
    var materialNumber: String? = "",
    var wbid: String? = "",
    var werks: String? = "",
    var werksName: String? = "0",
    var pchar: String? = "",
    var kpein: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var plantDesc: String? = "",
    var waers: String? = "",
    var bprme: String? = "",
    var matkl: String? = "",
    var totalPrice: String? = "",
    var unitPrice: String? = "",
    var qchar: String? = "",
    var meins: String? = "",
    var unitsOfMeasure: String? = "",
    var year: String? = "",
    var inventoryRes: String? = "",
    var finalApproval: String? = "",
    var isAdded: Boolean = false,
    var isChecked: Boolean = false,
    var poNumber: String? = ""
) : Parcelable
