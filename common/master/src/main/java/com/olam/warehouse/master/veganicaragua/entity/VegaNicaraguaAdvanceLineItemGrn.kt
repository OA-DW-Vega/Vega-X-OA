package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["tmpWbId", "documentNumber"])
data class VegaNicaraguaAdvanceLineItemGrn(
    var tmpWbId: String = "",
    var documentNumber: String = "",
    var financialYear: String? = "",
    var currency: String? = "",
    var postingDate: String? = "",
    var documentDate: String? = "",
    var date: String? = "",
    var baselineDate: String? = "",
    var indicator: String? = "",
    var businessArea: String? = "",
    var amount: String? = "",
    var companyCode: String? = "",
    var vendor: String? = "",
    var itemNum: String? = "",
    var advanceKnockAmount: String? = "",
    var totalAdvanceKnockAmount: String? = "",
    var interestAmount: String? = "",
    var commissionAmount: String? = "",
    var legalExpenseAmount: String? = "",
    var currencyDevaluationAmount: String? = ""
) : Parcelable
