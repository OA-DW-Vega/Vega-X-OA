package com.olam.warehouse.master.vegaecuador.model

import android.os.Parcelable
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Created by Keerthi Santhanam on 7/16/2020.
 */
data class VegaEcuadorDeliveryPost(
    val key: String,
    val plant: Plant,
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var deliveryDetails: List<VegaEcuadorDispatchLotsMerge> = emptyList()
)

data class VegaEcuadorDispatchPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaEcuadorDispatchPurchaseOrders> = emptyList()
)

@Parcelize
data class VegaEcuadorDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    var message: String = "",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false
) : Parcelable

data class VegaEcuadorDeliveryPostResponse(
    var delFlag: String? = "",
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var deliveryDetails: List<VegaEcuadorDispatchLotsMerge> = emptyList(),
    val key: String = getCurrentKey(),
    var pgi: Boolean = false,
    var pgiFlag: String? = "",
    var pickingBatchFlag: String? = "",
    var picking: Boolean = false,
    val plant: Plant,
    var message: String = ""
)

data class VegaEcuadorMtntDeliveryDetail(
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
    var isPgiFlag: Boolean = false,
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
    var endLotFlag: Boolean = false,
    var storageLossFlag: Boolean? = false,
    var vehicleNumber:String? = "",
    var msgList: List<String> = emptyList()
)
data class VegaEcuadorWbDeliveryPost(
    val key: String,
    val plant: Plant,
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var deliveryDetails: List<VegaEcuadorWbDispatchLotsMerge> = emptyList(),
    val weighmentType: String)

data class VegaEcuadorWbDispatchLots(
    var weighBridgeId: String = "",
    var batchNumber: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var netWeight: String? = "",
    var editedWeight: String? = "",
    var isAdded: Boolean? = false,
    var pairId: Int? = 0,
    var binBatch: String? = "",
    var isChecked: Boolean? = false,
    var processOrderNo: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    var remarks: String = "",
    var turnAroundTime: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var purchaseDocNum: String = "",
    var purchaseDocDesc: String = "",
    var deliveryStatus: Boolean = false,
    var encodedImageContent: String? = "",
    var imageUploadMsg: String? = "",
    var unitsOfMeasure: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    var isLowerWeight: Boolean = true,
    var delivery: String? = "",
    var grossWeight: String? = "",
    var endLotFlag: Boolean? = false,
    var postingDate: String? = "",
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList()
)


data class VegaEcuadorWbDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorWbDispatchLots> = ArrayList(),
    var message: String = "",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false
)

