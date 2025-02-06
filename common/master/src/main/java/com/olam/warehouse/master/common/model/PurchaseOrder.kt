package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan 24/6/2020.
 */
@Parcelize
data class PurchaseOrder(
    var poId: String = "",
    var warehouseId: String = "",
    var ebelp: String = "",
    var poType: String = "",
    var material: String = "",
    var supplier: String = "",
    var createdDate: String = "",
    var menge: String = "",
    var meins: String = "",
    var charg: String = "",
    var materialNumber: String = "",
    var materialDesc: String? = "",
    var supplierName: String = "",
    var supplierCode: String = "",
    var bsart: String = "",
    var ekorg: String = "",
    var ekgrp: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = "",
    var issueLocation: String? = "",
    var recPlantId: String = "",
    var openQuantity: String? = "",
    var unitPrice: String? = ""
) : Parcelable
