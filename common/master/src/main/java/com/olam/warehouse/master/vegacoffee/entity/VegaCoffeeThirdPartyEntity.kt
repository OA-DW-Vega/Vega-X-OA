package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

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
    var remarks: String? = ""
) : Parcelable
