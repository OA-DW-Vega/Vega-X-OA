package com.olam.warehouse.master.veganicaragua.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(primaryKeys = ["documentNumber", "vendor"])
data class VegaNicaraguaAdvanceLineItems(
    @ColumnInfo(index = true)
    var documentNumber: String = "",
    var financialYear: String? = "",
    var currency: String? = "",
    var postingDate: String? = "",
    var documentDate: String? = "",
    var baselineDate: String? = "",
    var indicator: String? = "",
    var businessArea: String? = "",
    var amount: String? = "",
    var itemNum: String? = "",
    var deletedFlag: Boolean? = false,
    var companyCode: String? = "",
    var vendor: String = ""
)
