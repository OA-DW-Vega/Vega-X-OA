package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

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
    var complainceFlag: String? = "",
    var price: Double? = 0.0,
    var currency: String? = "",
    var plant: String? = "",
    var languageCode: String? = "",
    var unitsOfMeasure: String? = "",
    var thirdPartyFlag: String? = "",
    var typeCode: String? = "",
    var eudrEquivalentMap: String? = "",
    var thirdPartyMaterialCode: String? = "",
    var productGroup: String? = "",
    var cropLimit: Double? = 0.0,

    ) : Parcelable
