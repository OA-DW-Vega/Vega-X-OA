package com.olam.warehouse.master.vegacocoa.model

import android.os.Parcelable
import androidx.room.Entity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import kotlinx.android.parcel.Parcelize


data class VegaCocoaDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var SplitFlag: Boolean = false,
    var deliveryDetails: List<VegaCocoaDispatchWB> = emptyList(),
    var binformFlag: Boolean = false
)


data class VegaCocoaPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaCocoaPurchaseOrders> = emptyList()
)

@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
@Parcelize
data class VegaCocoaPurchaseOrders(
    var purchaseDocNum: String = "",
    var purchaseOrderType: String? = "",
    var materialCode: String = "",
    var warehouseId: String = "",
    var purchaseDocDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var batchNumber: String? = "",
    var plantId: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var issueLocation: String? = "",
    var materialName: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var openQuantity: String? = "",
    var createdDate: String = ""
) : Parcelable

@Parcelize
data class VegaCocoaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaCocoaDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaCocoaDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
