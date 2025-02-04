package com.olam.warehouse.vegax.exportsalesindo.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */

@Parcelize
class VegaIndoCoffeeExportSalesOrderModel(
    var salesOrderId: String = "",
    var salesOrderList: List<IndoExporSalesMaterialList> = emptyList()
) : Parcelable


data class VegaIndoCoffeeExportSalesPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaIndoCoffeeExportSalesDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var plant: Plant
)

data class VegaIndoCoffeeExportSalesDeliveryDetail(
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
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var containerFlag: Boolean? = false,
    var containerNum: String? = "",
    var toVendorCode: String? = ""

)
