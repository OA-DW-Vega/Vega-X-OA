package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 10/5/2020.
 */
@Parcelize
@Entity
data class VegaNicaraguaInvoiceDetails(
    @PrimaryKey
    var tempId: String = "",
    var item: String? = "",
    var charg: String? = "",
    var materialName: String? = "",
    var materialNumber: String? = "",
    var supplierName: String? = "",
    var supplierCode: String? = "",
    var wbid: String = "",
    var werks: String? = "",
    var grn: String? = "",
    var discount: String? = "",
    var pchar: String? = "",
    var unitPrice: String? = "",
    var basePrice: String? = "",
    var kpein: String? = "",
    var totalPrice: String? = "",
    var grnQty: String? = "",
    var grnType: String? = "",
    var discountWeight: String? = "",
    var meins: String? = "",
    var waers: String? = "",
    var qchar: String? = "",
    var werksName: String? = "",
    var plantDesc: String? = "",
    var bprme: String? = "",
    var erdat: String? = "",
    var syncStatusMsg: String? = "",
    var isSynced: Boolean = false,
    var isChecked: Boolean = false,
    var qualityGrade: String? = "",
    var netPayment: String? = "",
    var advance: String? = "",
    var matkl: String? = "",
    var taxId: String? = "",
    var bagsCount: String? = "",
    var grossWeight: String? = "",
    var tareWeight: String? = "",
    var totalFTDC: String? = "",
    var totalVolumePremium: String? = "",
    var yieldPercentage: String? = "",
    //New fields
    var isReceiptData: Boolean? = false,
    var netWeight: String? = "",
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
    var exchangeRate: String? = "",
    var certification: String? = "",
    var invoiceNo: String? = ""
) : Parcelable
