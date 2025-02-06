package com.olam.warehouse.vegax.mtntsesame.data.domain.model

import android.os.Parcelable
import androidx.room.Entity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import kotlinx.parcelize.Parcelize
import java.util.*


data class VegaNigeriaSesameMtntDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaNigeriaSesameMtntDeliveryDetail> = emptyList(),
    val weighmentType: String
)

data class VegaNigeriaSesameMtntMergedDeliveryPost(
    val key: String,
    val plant: Plant,
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var vehicleNumber: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var deliveryDetails: List<VegaNigeriaSesameMtntDispatchLotsMerge> = emptyList()
)

@Parcelize
data class VegaNigeriaSesameMtntDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    var message: String = "",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false
) : Parcelable

data class VegaNigeriaSesameMtntPurchaseOrder(
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
data class VegaNigeriaSesameMtntDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaNigeriaSesameMtntDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaNigeriaSesameMtntDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
