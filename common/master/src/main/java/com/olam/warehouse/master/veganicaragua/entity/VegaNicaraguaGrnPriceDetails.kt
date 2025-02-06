package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.parcelize.Parcelize


@Entity(primaryKeys = ["fieldName"])
@Parcelize
data class VegaNicaraguaGrnPriceDetails(

    @ColumnInfo(index = true)
    var fieldName: String = "",
    var companyCode: String? = "",
    var division: String? = "",
    var plant: String? = "",
    var purchasingOrg: String? = "",
    var purchasingGroup: String? = "",
    var description: String? = "",
    var priceDate: String? = "",
    var price: String? = "",
    var differential: String? = "",
    var currency: String? = "",
    var baseUnit: String? = "",
    var createdOn: String? = "",
    var percentage: String? = ""
): Parcelable
