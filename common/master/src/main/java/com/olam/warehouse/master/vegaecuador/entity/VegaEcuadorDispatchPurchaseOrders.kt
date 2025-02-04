package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
@Parcelize
data class VegaEcuadorDispatchPurchaseOrders(
    var purchaseDocNum: String = "",
    var purchaseOrderType: String? = "",
    var materialCode: String = "",
    var purchaseDocDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var batchNumber: String? = "",
    var plantId: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var materialName: String? = "",
    var openQuantity: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var bsart: String? = "",
    var warehouseId: String? = "",
    var ekgrp: String? = "",
    var ekorg: String? = ""
) : Parcelable
