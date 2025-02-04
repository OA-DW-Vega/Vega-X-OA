package com.olam.warehouse.vegax.offloadingcoffee.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import kotlinx.android.parcel.Parcelize

data class VegaCoffeeReceivingPostLineItem(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceivingLineItem>
)

data class VegaCoffeeWeighScalePallet(
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

@Parcelize
data class VegaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityGrade: String? = "",
    var certification: String? = ""
) : Parcelable
