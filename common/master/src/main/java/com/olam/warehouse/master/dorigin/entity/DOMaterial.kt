package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity
data class DOMaterial(
    @PrimaryKey
    var materialCode: String = "",
    var materialName: String? = "",
    var complainceFlag: String? = "",
    var price: String? = "",
    var currency: String? = "",
    var plant: String? = "",
    var languageCode: String? = "",
    var unitsOfMeasure: String? = "",
//    var isBltEnabled: Boolean? = false,
    var bltEnabled: Boolean? = false,
    var scanLevelId: Int? = null,
    var scanLevelName: String? = ""
)
