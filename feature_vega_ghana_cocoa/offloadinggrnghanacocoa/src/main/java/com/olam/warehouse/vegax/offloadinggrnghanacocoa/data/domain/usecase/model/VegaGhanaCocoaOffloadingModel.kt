package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model

import android.os.Parcelable
import androidx.room.Entity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import kotlinx.parcelize.Parcelize
import java.util.ArrayList

data class VegaGhanaReceivingPostLineItem(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceivingLineItem>
)

data class VegaGhanaWeighScalePallet(
    var weighbridgeID: String? = "",
    var serialNumber: String? = "",
    var plant: String? = "",
    var material: String? = "",
    var batch: String? = "",
    var gossWeight: Boolean = false,
    var packingWeight: String? = "",
    var netWeight: String? = "",
    var packingMaterial1: String? = "",
    var noofPackingMat1: String? = "",
    var packingMatweight1: String? = "",
    var packingMaterial2: String? = "",
    var noofPackingMat2: String? = "",
    var packingMatweight2: String? = "",
    var unit: String? = "",
    var storageLocation: String? = "",
    var noOfPallet: String? = "",
    var totalPalletWeight: String? = ""
)

data class VegaSendingLocation(
    var plant: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = ""
)
data class VegaGhanaReasonModel(
    var item :List<VegaGhanaCocoaRejectReason>
)
data class VegaGhanaCocoaRejectReason(
    var rejectReason: String = "",
    var reasonCode: Long
)
data class VegaGhanaMtntLotListModel(
    var selectedList: ArrayList<VegaGhanaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false
)

data class VegaGhanaGRNDSELotListModel(
    var selectedList: ArrayList<VegaGRNDWLotManualModel> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var isThirdParty: Boolean = false
)



@Entity(primaryKeys = ["purchaseDocNum", "materialCode"])
@Parcelize
data class VegaGhanaGrnPurchaseOrders(
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
    var issueLocation: String? = "",
    var materialName: String? = "",
    var menge: String? = "",
    var meins: String? = "",
    var warehouseId: String? = "",
    var openQuantity: String? = ""
) : Parcelable

data class ValidateNumber(
    var msgtype:String?="",
    var message:String?="",
    var success: Boolean
)
