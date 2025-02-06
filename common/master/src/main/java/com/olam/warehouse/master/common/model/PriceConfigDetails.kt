package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant

data class PriceConfigDetails(
    val id: Int? = 0,
    val plant: Plant? = null,
    val materialDetail: Material? = null,
    val grade: Grade? = null,
    val qualityCode: String? = "",
    val certificatePremium: String? = "",
    val volumePremiumFrom: String? = "",
    val volumePremiumTo: String? = "",
    val volumePremium: String? = "",
    val moisturePremium: String? = "",
    val qualityDiscount: String? = "",
    val moistureDiscount: String? = "",
    val dailyPrice: String? = "",
    val exportIncentive: String? = "",
    val pricingCommission: PricingCommission? = null,
    val createdAt: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = "",
    val ftdc: String? = ""
)

data class PricingCommission(
    val id: Int? = 0,
    val withHoldingTax: String? = "",
    val neCommission: String? = "",
    val bankCommsion: String? = "",
    val ftdc: String? = "",
    val createdAt: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = ""
)

data class Grade(
    val id: Int? = 0,
    val grade: String? = "",
    val gradeCode: String? = "",
    val createdAt: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = ""
)


