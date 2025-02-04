package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
data class VegaReceivingWarehouse(
    @PrimaryKey
    var supplyingPlantId: String = "",
    var supplyingPlantName: String = "",
    var isExpanded: Boolean? = false
)
