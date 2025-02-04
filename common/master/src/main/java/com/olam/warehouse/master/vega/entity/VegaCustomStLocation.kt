package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.Ignore

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */

@Entity(primaryKeys = ["procureLocationCode", "plant"])
data class VegaCustomStLocation(
    var procureLocationCode: String = "",
    var plant: String = "",
    var procureLocationName: String? = "",
    var storageLocationType: String? = "",
    @Ignore
    var binDetails: List<VegaBinDetails>? = emptyList()
)

