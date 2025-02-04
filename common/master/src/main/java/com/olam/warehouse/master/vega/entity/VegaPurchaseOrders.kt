package com.olam.warehouse.master.vega.entity

import androidx.room.Entity
import androidx.room.Ignore

/**
 * Created by Baskaran Kannan on 2/18/2020.
 */
@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
data class VegaPurchaseOrders(
    var purchaseDocNum: String = "",
    var purchaseOrderType: String? = "",
    var materialCode: String = "",
    var purchaseDocDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var batchNumber: String? = "",
    var plantId: String? = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = "",
    var materialName: String? = "",
    //new field
    var menge: String? = "",
    var meins: String? = "",
    var warehouseId: String? = "",
    @Ignore
    var openQuantity: String? = "",
    @Ignore
    var createdDate: String = ""

)

data class VegaPurchaseOrderModel(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaPurchaseOrders> = emptyList()
)
