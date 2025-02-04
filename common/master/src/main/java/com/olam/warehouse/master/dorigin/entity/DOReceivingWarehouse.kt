package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity
data class DOReceivingWarehouse(
    @PrimaryKey
    var supplyingPlantId: String = "",
    var supplyingPlantName: String = ""
)
