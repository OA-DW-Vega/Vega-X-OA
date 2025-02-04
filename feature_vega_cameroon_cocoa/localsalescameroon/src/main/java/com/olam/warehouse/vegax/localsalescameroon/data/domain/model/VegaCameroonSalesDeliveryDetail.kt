package com.olam.warehouse.vegax.localsalescameroon.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial

data class VegaCameroonSalesPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaCameroonSalesDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var plant: Plant
)

data class VegaCameroonSalesDeliveryDetail(
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
    var mergedBatchNumber: String? = "",
    var toVendorCode: String? = "",
    var bagList: List<VegaCoffeeSalesBagMaterial> = emptyList()
)

data class VegaCameroonSalesPallet(
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
