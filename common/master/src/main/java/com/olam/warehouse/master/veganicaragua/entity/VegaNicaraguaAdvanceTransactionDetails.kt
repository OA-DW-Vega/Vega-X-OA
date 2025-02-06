package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["tempId"])
class VegaNicaraguaAdvanceTransactionDetails (
    var tempId: String = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var availableLimit: String? = "",
    var availableLimitSign: String? = "",
    var creditLimit: String? = "",
    var exchangeRate: String? = "",
    var requestedAdvanceAmount: String? = "",
    var tenureInDays: String? = "",
    var promissoryNumber: String? = "",
    var maturityDate: String? = "",
    var documentNumber: String? = "",
    var accountingNumber: String? = "",
    var syncStatusMsg: String? = "",
    var syncStarted: Boolean? = false,
    var isOfflineData: Boolean? = false,
    var syncStatus: Boolean? = false,
    var isLocalCurrency: Boolean? = false,
    var createdDate: String? = "",
    var docDate: String? = "",
    var erdat: String? = ""
): Parcelable
