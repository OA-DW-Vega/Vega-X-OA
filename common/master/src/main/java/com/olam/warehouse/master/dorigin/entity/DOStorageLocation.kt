package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity(primaryKeys = ["plant", "storageLocationCode"])
data class DOStorageLocation(
    var plant: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = ""
)
