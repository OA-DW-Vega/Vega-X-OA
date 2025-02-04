package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MtntPurchaseOrder(
    var poId: String = "",
    var poType: String? = "",
    var material: String = "",
    var bsart: String? = "",
    var ebelp: String? = "",
    var ekgrp: String? = "",
    var ekorg: String? = "",
    var charg: String? = "",
    var recPlantId: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var materialName: String? = "",
    var openQuantity: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var materialDesc: String? = "",
    var materialNumber: String? = "",
    var supplier: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var warehouseId: String? = ""
): Parcelable

