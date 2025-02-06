package com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Created by Baskaran Kannan on 9/3/2020.
 */
@Parcelize
class VegaNigeriaExportSalesOrderModel(
    var salesOrderId: String = "",
    var salesOrderList: List<materialList> = emptyList()
) : Parcelable

@Parcelize
data class materialList(
    var salesOrderId: String? = "",
    var salesItemNum: String? = "",
    var materialNumber: String? = "",
    var materialDesc: String? = "",
    var plantId: String? = "",
    var vkorg: String? = "",
    var vtweg: String? = "",
    var Spart: String? = "",
    var soldToPartyName: String? = "",
    var createdDate: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var openQuantity: String? = "",
    var soldToPartyCode: String? = "",
    var shipToPartyName: String? = "",
    var shipToPartyCode: String? = ""
) : Parcelable

data class VegaNigeriaExportSalesPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaNigeriaExportSalesDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var textNavListValues: List<VegaNigeriaExportSalesTextDetail>,
    var plant: Plant
)

data class VegaNigeriaExportSalesAssignLot(
    var containerNumber: String? = "",
    var materialName: String? = "",
    var mergedLotId: String? = ""
)

@Parcelize
data class VegaNigeriaExportSalesTextDetail(
    var textId: String? = "",
    var textValue: String? = ""
) : Parcelable

data class VegaNigeriaExportSalesDeliveryDetail(
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
    var toVendorCode: String? = "",
    var mergedBatchNumber: String? = "",
    var storageLossFlag: Boolean? = false
)
data class VegaNigeriaInventoryModel(
    var containerDTOs: List<NigeriaContainerInventory>,
    var message :String? = ""
)

data class NigeriaContainerInventory(
    var id: Int = 0,
    var containerNum: String ="",
    var status: String,
    var containerWeight: String,
    var containerSize: String,
    var shippingLine: String,
    var entryDate: String,
    var uom: String
) : Serializable

data class VegaNigeriaContainerStatusUpdate(
    var companyCodeDto: String? = "",
    var containerNum: String = "",
    var status: String? = ""
)

data class VegaNigeriaContainerUpdate(
    var containerNum: String? = "",
    var key: String = "",
    var plantId: String? = "",
    var status: String? = ""
)

data class VegaNigeriaSalesTypeModel(
    val TEXT_UPDATE: List<VegaNigeriaSalesType>
)

data class VegaNigeriaSalesType(
    var BILLOFLADINGNUMBER: String? = "",
    var PORTOFLOADING: String? = "",
    var PORTOFDISCHARGEDETAILED: String? = "",
    var BILLOFLADINGDATE: String? = "",
    var VESSELNAMEFLIGHTNO: String? = "",
    var DELIVEREDQTY: String? = "",
    var SHIPPINGLINE: String? = ""
)

