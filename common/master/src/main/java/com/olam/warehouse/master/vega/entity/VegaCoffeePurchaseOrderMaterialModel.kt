package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = ["materialCode"])
@Parcelize
data class VegaCoffeePurchaseOrderMaterialModel(

    var weighBridgeId: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var soWeight: String? = "",
    var uom: String? = "",
    var soNumber: String? = "",
    var dispatchWeight: String? = "",
    var purchaseOrderNum: String = "",
    var purchaseOrderDesc: String = ""
) : Parcelable

@Entity(primaryKeys = ["weighBridgeId", "materialCode"])
@Parcelize
data class VegaGhanaPurchaseOrderMaterialModel(

    var weighBridgeId: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var soWeight: String? = "",
    var uom: String? = "",
    var soNumber: String? = "",
    var dispatchWeight: String? = "",
    var purchaseOrderNum: String? = "",
    var purchaseOrderDesc: String? = ""
) : Parcelable
