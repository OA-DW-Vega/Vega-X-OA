package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */

@Entity
class VegaWarehouse(
    @PrimaryKey
    var warehouseId: String = "",
    var warehouseName: String? = "",
    var plantId: Long? = 0
)
