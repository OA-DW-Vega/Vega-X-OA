package com.olam.warehouse.vegax.dispatchnigeria.data.domain.model

import android.os.Parcelable
import androidx.room.Entity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import kotlinx.parcelize.Parcelize
import java.util.*


data class VegaNigeriaCocoaMtntDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaNigeriaCocoaMtntDeliveryDetail> = emptyList(),
    val weighmentType: String
)

data class VegaNigeriaCocoaMtntBinMerge(
    val key: String,
    val plant: Plant,
    var deliveryDetails: List<VegaNigeriaCocoaMtntDispatchLotsBinMerge> = emptyList()
)

@Parcelize
data class VegaNigeriaCocoaMtntDispatchLotsBinMerge(
    var batchNumber: String = "",
    var materialCode: String? = "",
    var plantId: String? = "",
    var postingDate: String? = "",
    var netWeight: String? = "",
    var mergedBatchNumber: String? = "",
    var recStorageLocationCode: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var storageLossFlag: Boolean = false
) : Parcelable

data class VegaNigeriaCocoaMtntMergedDeliveryPost(
    val key: String,
    val plant: Plant,
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var vehicleNumber: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var deliveryDetails: List<VegaNigeriaCocoaMtntDispatchLotsMerge> = emptyList(),
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = "",
    var environment: String? = ""

)

@Parcelize
data class VegaNigeriaCocoaMtntDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    var message: String = "",
    var netWeight:String="0",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false
) : Parcelable

data class VegaNigeriaCocoaMtntPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaNigeriaSesameMtntPurchaseOrders> = emptyList()
)

@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
@Parcelize
data class VegaNigeriaSesameMtntPurchaseOrders(
    var purchaseDocNum: String? = "",
    var purchaseOrderType: String? = "",
    var materialCode: String = "",
    var purchaseDocDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var batchNumber: String? = "",
    var plantId: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var materialName: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var warehouseId: String? = "",
    var openQuantity: String? = ""
) : Parcelable

@Parcelize
data class VegaNigeriaCocoaMtntDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaNigeriaCocoaMtntDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaNigeriaCocoaMtntDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
