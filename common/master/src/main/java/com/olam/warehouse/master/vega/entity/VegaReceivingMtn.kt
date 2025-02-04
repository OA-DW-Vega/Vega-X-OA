package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
data class VegaReceivingMtn(
    @PrimaryKey
    var mtnNumber: String = "",
    var batch: String = "",
    var posnr: String = "",
    var weight: String = "",
    var uom: String = "",
    var materialNumber: String = "",
    var materialName: String = "",
    var purchaseOrder: String = "",
    var ebelp: String = "",
    var supplyingPlantId: String = "",
    var storageLocationCode: String = "",
    var supplyingPlantName: String = "",
    var mtntWbid: String = "",
    var gateEntry: String? = "",
    var isSynced: Boolean? = false,
    var mtntDate: String? = ""
)
