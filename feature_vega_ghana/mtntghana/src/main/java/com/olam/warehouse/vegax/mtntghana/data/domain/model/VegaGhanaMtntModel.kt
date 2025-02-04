package com.olam.warehouse.vegax.mtntghana.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import kotlinx.android.parcel.Parcelize
import java.util.*


data class VegaGhanaMtntLotListModel(
    var selectedList: ArrayList<VegaGhanaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false
)

data class VegaGhanaMtntPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaGhanaMtntDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var plant: Plant
)
@Parcelize
data class VegaGhanaDispatchLotsMerge(
    var batchNumber: String = "",
    var mergeStatus: String = "",
    var lots: ArrayList<VegaEcuadorDispatchLots> = ArrayList(),
    var message: String = "",
    var deliveryId: String = "",
    var delivery: Boolean = false,
    var pgi: Boolean = false,
    var picking: Boolean = false,
    var storageLossFlag :Boolean = false
) : Parcelable


data class VegaGhanaMergedDeliveryPostResponse(
    var delFlag: String? = "",
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var deliveryDetails: List<VegaGhanaDispatchLotsMerge> = emptyList(),
    val key: String = getCurrentKey(),
    var pgi: Boolean = false,
    var pgiFlag: String? = "",
    var pickingBatchFlag: String? = "",
    var picking: Boolean = false,
    val plant: Plant,
    var message: String = ""
)

data class VegaGhanaMtntDeliveryDetail(
    var batchNumber: String? = "",
    var turnAroundTime: String = "",
    var bltxt: String? = "",
    var createdDate: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var deliveryStatus: Boolean = false,
    var endTime: String? = "",
    var frbnr1: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
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
    var documentNum: String? = "",
    var vehicleNumber: String? = "",
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var isPgiFlag: Boolean = false,
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
    var endLotFlag: Boolean = false,
    var storageLossFlag: Boolean? = false,
    var soWeight: String? = "",
    var msgList: List<String> = emptyList()
)

data class VegaGhanaMtntPallet(
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

data class VegaGhanaMtntWeighScalePallet(
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
