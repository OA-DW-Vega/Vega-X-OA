package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.veganicaragua.entity.GrnDetails


data class GrnPriceDetails(
    val companyCode: String? = "",
    val division: String? = "",
    val plant: String? = "",
    val purchasingOrg: String? = "",
    val purchasingGroup: String? = "",
    val fieldName: String? = "",
    val description: String? = "",
    val priceDate: String? = "",
    val price: String? = "",
    val differential: String? = "",
    val currency: String? = "",
    val baseUnit: String? = "",
    val createdOn: String? = ""
)

data class VendorGrnDetails(
    val grnDetails: List<GrnDetails>? = emptyList(),
    val vendor: String? = ""
)
