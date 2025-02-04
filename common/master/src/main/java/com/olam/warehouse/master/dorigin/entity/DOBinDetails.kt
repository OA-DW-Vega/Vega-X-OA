package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */

@Entity
data class DOBinDetails(
    @PrimaryKey
    var binLocationCode: String = "",
    var procureLocationCode: String? = "",
    var binLocationName: String? = ""
)
