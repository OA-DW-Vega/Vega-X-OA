package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vega.entity.VegaBinDetails

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */

@Entity
data class DOCustomStLocation(
    @PrimaryKey
    var procureLocationCode: String = "",
    var plant: String? = "",
    var procureLocationName: String? = "",
    var storageLocationType: String? = "",
    @Ignore
    var binDetails: List<VegaBinDetails>? = emptyList()
)

