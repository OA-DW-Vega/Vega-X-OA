package com.olam.warehouse.master.veganicaragua.entity

import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["materialCode", "qualityCode"])
data class VegaNicaraguaPriceConfigDetails(
    var id: Int? = 0,
    @Ignore
    var plant: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    @Ignore
    var materialDetail: String? = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var grade: String? = "",
    var qualityCode: String = "",
    var certificatePremium: String? = "",
    var volumePremiumFrom: String? = "",
    var volumePremiumTo: String? = "",
    var volumePremium: String? = "",
    var moisturePremium: String? = "0.0",
    var qualityDiscount: String? = "",
    var moistureDiscount: String? = "",
    var dailyPrice: String? = "",
    var exportIncentive: String? = "",
    @Ignore
    var pricingCommission: String? = "",
    var withHoldingTax: String? = "",
    var neCommission: String? = "",
    var bankCommsion: String? = "",
    var ftdc: String? = ""
)

