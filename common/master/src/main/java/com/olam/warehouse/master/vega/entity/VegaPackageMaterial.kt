package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 1/10/2020.
 */
@Entity
data class VegaPackageMaterial(
    @PrimaryKey
    var bagType: String = "",
    var weightType: String = "",
    var bagMaterialCode: String = "",
    var standardWeight: String? = "0",
    var tareWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var tolerance: String? = "",
    var plant: String? = "",
    var isDefault: Boolean? = false
)
