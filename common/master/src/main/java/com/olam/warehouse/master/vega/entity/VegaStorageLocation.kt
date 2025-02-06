package com.olam.warehouse.master.vega.entity

import androidx.room.Entity

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */

@Entity(primaryKeys = ["plant", "storageLocationCode"])
data class VegaStorageLocation(
    var plant: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = ""
)
