package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 4/1/2020.
 */
@Entity
data class VegaBcZoneMapping(
    @PrimaryKey
    var sapuserId: String = "",
    var bczone: String? = ""
)
