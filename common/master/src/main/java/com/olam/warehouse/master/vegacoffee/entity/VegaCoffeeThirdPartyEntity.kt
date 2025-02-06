package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = ["vendorWithTransferType"])
@Parcelize
data class VegaCoffeeThirdPartyRequestModel(
    var materialCode: String = "",
    var materialName: String = "",
    var fromVendorName: String = "",
    var fromVendorCode: String = "",
    var toVendorCode: String? = "",
    var toVendorName: String? = "",
    var grnPrice: String? = "",
    var createDate: String? = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var transferType: String = "",
    var vendorWithTransferType: String = "",
    var remarks: String? = "",
    @Ignore
    var procureType: String = "",
    @Ignore
    var poDetails: PODetails? = null,
    @Ignore
    var eudrStatus: String = ""
) : Parcelable



@Parcelize
data class PODetails(
    var poId: String = "",
    var menge: String = "",
    var meins: String = "",
    var storageLocationCode: String = "",
    var openQuantity: String? = "",
    var unitPrice: String? = "",
    var bsart: String = "",
    var ekorg: String = "",
    var ekgrp: String = "",
    var ebelp: String = "",
    var poType: String = "",
    var supplierCode: String = "",
) : Parcelable
