package com.olam.warehouse.vegax.mtntcameroon.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import kotlinx.android.parcel.Parcelize


data class VegaCameroonDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaCameroonMtntDeliveryDetail> = emptyList(),
    val weighmentType: String
)


data class VegaCameroonPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaCameroonPurchaseOrders> = emptyList()
)


@Parcelize
data class VegaCameroonPurchaseOrders(
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
    var warehouseId: String? = ""
) : Parcelable

@Parcelize
data class VegaCameroonDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaCameroonDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaCameroonDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
