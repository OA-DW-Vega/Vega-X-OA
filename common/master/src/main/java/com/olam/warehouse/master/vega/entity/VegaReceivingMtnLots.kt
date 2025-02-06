package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import java.io.Serializable

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity(primaryKeys = ["batch", "mtnNumber"])
data class VegaReceivingMtnLots(
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
    var bagCount: Int = 0,
    var storageLocationCode: String? =""
): Serializable
