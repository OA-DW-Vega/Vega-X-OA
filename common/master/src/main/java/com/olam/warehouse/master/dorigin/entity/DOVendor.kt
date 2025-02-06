package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity
data class DOVendor(
    @PrimaryKey
    var vendorCode: String = "",
    var vendorAddress: String? = "",
    var vendorCity: String? = "",
    var countryCode: String? = "",
    var vendorName: String? = ""
)
