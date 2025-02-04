package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/10/2020.
 */
@Parcelize
@Entity
data class VegaMaterial(
    @PrimaryKey
    var materialCode: String = "",
    var materialName: String? = "",
    var materialType: String? = "",
    var price: Double? = 0.0,
    var currency: String? = "",
    var plant: String? = "",
    var languageCode: String? = "",
    var unitsOfMeasure: String? = "",
    var thirdPartyFlag: String? = "",
    var typeCode: String? = "",
    var thirdPartyMaterialCode: String? = ""
) : Parcelable
