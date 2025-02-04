package com.olam.warehouse.vegax.offloadingindo.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */

data class VegaIndoCoffeeReceivingPostLineItem(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceivingLineItem>
)

data class VegaIndoCoffeeWeighScalePallet(
    var weighbridgeID: String? = "",
    var serialNumber: String? = "",
    var plant: String? = "",
    var material: String? = "",
    var batch: String? = "",
    var gossWeight: Boolean = false,
    var packingWeight: String? = "",
    var netWeight: String? = "",
    var packingMaterial1: String? = "",
    var noofPackingMat1: String? = "",
    var packingMatweight1: String? = "",
    var packingMaterial2: String? = "",
    var noofPackingMat2: String? = "",
    var packingMatweight2: String? = "",
    var unit: String? = "",
    var storageLocation: String? = "",
    var noOfPallet: String? = "",
    var totalPalletWeight: String? = ""
)
