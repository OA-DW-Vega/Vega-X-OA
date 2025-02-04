package com.olam.warehouse.vegax.salescoffee.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import kotlinx.android.parcel.Parcelize

data class VegaCoffeeSalesPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaCoffeeSalesDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var plant: Plant
)

@Parcelize
data class VegaCocoaSummary(
    var remark: String,
    var duration: String,
    var purchaseOrder: VegaCocoaDispatchWB?,
    var lots: List<VegaCocoaDispatchLots>,
    var isThirdParty: Boolean = false
) : Parcelable

data class VegaCoffeeSalesDeliveryDetail(
    var batchNumber: String? = "",
    var bltxt: String? = "",
    var createdDate: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var deliveryStatus: Boolean = false,
    var endTime: String? = "",
    var frbnr1: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
    var msgList: List<String>? = emptyList(),
    var netWeight: String? = "",
    var plantId: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    var remarks: String? = "",
    var salesItem: String? = "",
    var salesOrderNum: String? = "",
    var startTime: String? = "",
    var turnAroundTime: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var updatedDate: String? = "",
    var wayBillNo: String? = "",
    var weighBridgeId: String? = "",
    var huno: String? = "", // bag uom
    var huwt: String? = "", // bag tare weight
    var huno2: String? = "", //pallet uom
    var huwt2: String? = "", // pallet tare weight
    var nohu1: String? = "", // no of bags
    var nohu2: String? = "", // no of pallet
    var grossWeight: String? = "",
    var year: String? = "",
    var documentNum: String? = "",
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var storageLossFlag: Boolean? = false,
    var endLotFlag: Boolean? = false,
    var bagList: List<VegaCoffeeSalesBagMaterial> = emptyList(),
    var qualityDetails: List<VegaQualityParams>? = null,
    var truckDirection: String? = ""
)

data class VegaCoffeeSalesPallet(
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
