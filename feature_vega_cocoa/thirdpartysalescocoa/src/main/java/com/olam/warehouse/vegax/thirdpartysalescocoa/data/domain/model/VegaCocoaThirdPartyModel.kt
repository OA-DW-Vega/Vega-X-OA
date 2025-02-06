package com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial

data class VegaCoffeeThirdPartyLotListModel(
    var selectedList: ArrayList<VegaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false,
    var vendorCode: String = ""
)

data class VegaCoffeeTPDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    val grnPricePerUnit: String = "",
    val totalGrnPrice: String = "",
    var deliveryDetails: List<VegaCoffeeTPDeliveryDetail> = emptyList()
)

data class VegaCoffeeTPDeliveryDetail(
    var batchNumber: String? = "",
    var bltxt: String? = "",
    var deliveryStatus: Boolean = false,
    var frbnr1: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
    var netWeight: String? = "",
    var plantId: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    var remarks: String? = "",
    var salesItem: String? = "",
    var salesOrderNum: String? = "",
    var startTime: String? = "",
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
    var toVendorCode: String = "",
    var fromVendorCode: String = "",
    var grnPrice: String = "",
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList()
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

data class VegaCoffeeWSBagModel(
    var weight: String = "",
    var isPalletMatched: Boolean = true
)
