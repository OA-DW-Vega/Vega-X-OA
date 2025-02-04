package com.olam.warehouse.master.veganicaragua.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VegaNicaraguaInvoicePriceInfo(
    var netWeight: String? = "",
    var basePrice: String? = "",
    var totalPrice: String? = "",
    var certificatePremium: String? = "",
    var volumePremium: String? = "",
    var humidityPremium: String? = "",
    var qualityDiscounting: String? = "",
    var humidityDiscounting: String? = "",
    var grossValue: String? = "",
    var exportnCentives: String? = "",
    var withholdingTax: String? = "",
    var NSExchangeRate: String? = "",
    var bankCommission: String? = "",
    var FTDC: String? = "",
    var totalDduction: String? = "",
    var netPayment: String? = "",
    var finalPayment: String? = "",
    var currency: String? = "",
    var grossValuePerKg: String? = "",
    var qualityGradeDesc: String? = "",
    var netWeightQQs: String? = "",
    var advanceSummary: String? = "0.00",
    var advanceInterestSummary: String? = "0.00",
    var advanceCommissionSummary: String? = "0.00",
    var advanceLegalExpenseSummary: String? = "0.00",
    var advanceMaintainceSummary: String? = "0.00",
    var totalAdvanceSummary: String? = "0.00",
    var exchangeRate: String? = ""
) : Parcelable

