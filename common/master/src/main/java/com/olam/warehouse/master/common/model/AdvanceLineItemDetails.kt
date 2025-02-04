package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AdvanceLineItemDetails(

    var documentNumber: String? = "",
    var financialYear: String? = "",
    var currency: String? = "",
    var postingDate: String? = "",
    var documentDate: String? = "",
    var baselineDate: String? = "",
    var indicator: String? = "",
    var businessArea: String? = "",
    var amount: String? = "",
    var companyCode: String? = "",
    var vendor: String? = "",
    var itemNum: String? = "",
    var deletedFlag: Boolean? = false,
    var advanceKnockAmount: String? = "",
    var totalAdvanceKnockAmount: String? = "",
    var interestAmount: String? = "",
    var commissionAmount: String? = "",
    var legalExpenseAmount: String? = "",
    var currencyDevaluationAmount: String? = ""
) : Parcelable
