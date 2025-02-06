package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity(primaryKeys = ["vendorCode", "purchaseOrgType", "werks"])
@Parcelize
data class VegaVendor(
    var vendorCode: String = "",
    var vendorAddress: String? = "",
    var vendorCity: String? = "",
    var countryCode: String? = "",
    var vendorName: String? = "",
    var vendorType: String? = "",
    var vendorAdvLimit: String? = "",
    var bcApprover: String? = "",
    var vendorCustomerCode: String? = "",
    var purchaseOrgType: String = "",
    var taxNumber: String? = "",
    var storagelocationcodeName: String? = "",
    var werks: String = "123"
) : Parcelable
