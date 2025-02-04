package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity
data class DOPackageMaterial(
    @PrimaryKey
    var bagType: String = "",
    var weightType: String = "",
    var standardWeight: String? = "",
    var tareWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var tolerance: String? = "",
    var plant: String? = "",
    var isDefault: Boolean? = false
)
