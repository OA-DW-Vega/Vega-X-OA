package com.olam.warehouse.master.dorigin.entity

import androidx.room.Entity

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity(primaryKeys = ["batch", "mtnNumber"])
data class DOReceivingMtnLots(
    var batch: String = "",
    var materialNumber: String = "",
    var mtnNumber: String = "",
    var materialName: String = "",
    var posnr: String = "",
    var weight: Double = 0.0,
    var weightAdded: Double = 0.0,
    var uom: String = "",
    var purchaseOrder: String = "",
    var ebelp: String = "",
    var supplyingPlantId: String = "",
    var supplyingPlantName: String = "",
    var hasWeightAdded: Boolean = false,
    var bagCount: Int = 0
)
