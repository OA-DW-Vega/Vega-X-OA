package com.olam.warehouse.vegax.mtntcoffee.data.domain.model

import android.os.Parcelable
import androidx.room.Entity
import com.olam.warehouse.master.user.model.Plant
import kotlinx.parcelize.Parcelize


data class VegaCoffeeDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    val vehicleNumber: String?="",
    val driverName: String?="",
    val contactNumber: String?="",
    var deliveryDetails: List<VegaCoffeeMtntDeliveryDetail> = emptyList(),
    val weighmentType: String
)


data class VegaCoffeePurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaCoffeePurchaseOrders> = emptyList()
)

@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
@Parcelize
data class VegaCoffeePurchaseOrders(
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
    var meins: String? = ""
) : Parcelable

@Parcelize
data class VegaCoffeeDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaCoffeeDispatchLotQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaCoffeeDispatchLotQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable
