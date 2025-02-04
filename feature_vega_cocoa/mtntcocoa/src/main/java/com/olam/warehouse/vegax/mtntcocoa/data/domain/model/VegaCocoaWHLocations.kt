package com.olam.warehouse.vegax.mtntcocoa.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import kotlinx.android.parcel.Parcelize


@Parcelize
data class VegaCocoaSummary(
    var remark: String,
    var duration: String,
    var purchaseOrder: VegaCocoaDispatchWB?,
    var lots: List<VegaCocoaDispatchLots>,
    var isThirdParty: Boolean = false
) : Parcelable

data class VegaCocoaLotListModel(
    var selectedList: ArrayList<VegaCocoaNoWeighmentLot> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false
)

data class VegaCocoaNoWeighmentPallet(
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

data class VegaCocoaNWBagModel(
    var weight: String = "",var truckOut:String,
    var isPalletMatched: Boolean = true
)
