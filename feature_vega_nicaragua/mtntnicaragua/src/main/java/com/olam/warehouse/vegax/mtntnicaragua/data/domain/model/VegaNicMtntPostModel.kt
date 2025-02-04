package com.olam.warehouse.vegax.mtntnicaragua.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial

/**
 * Created by Baskaran Kannan on 12/7/2020.
 */

data class VegaNicMtntDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaNicMtntDeliveryDetail> = emptyList(),
    val weighmentType: String,
    val contactNumber: String,
    val driverName: String,
    val vehicleNumber: String,
    val remarks: String,
    val vehicleType: String,
    val mergedNetWeight: String?=""
)

data class VegaNicMtntDeliveryDetail(
    var batchNumber: String? = "",
    var turnAroundTime: String = "",
    var bltxt: String? = "",
    var createdDate: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var deliveryStatus: Boolean = false,
    var endTime: String? = "",
    var frbnr1: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
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
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var pgiFlag: Boolean = false,
    var bagList: List<VegaNicaraguaWeighmentBagMaterial> = emptyList(),
    var endLotFlag: Boolean = false,
    var storageLossFlag: Boolean? = false,
    var msgList: List<String> = emptyList()
)

data class VegaNicPurchaseOrderModel(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaCocoaPurchaseOrders> = emptyList()
)
