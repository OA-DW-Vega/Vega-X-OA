package com.olam.warehouse.vegax.dispatchnigeria.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import kotlinx.android.parcel.Parcelize
import java.util.*


data class VegaNigeriaCocoaMtntLotListModel(
    var selectedList: ArrayList<VegaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false
)

data class VegaNigeriaCocoaWeightedAveragePost(
    var key: String? = "",
    var batchUpdateFlag: Boolean = false,
    var weightedAvgFlag: Boolean = false,
    var plant: Plant,
    var deliveryDetails: List<VegaNigeriaCocoaWeightedAverageDeliveryDetail>,
)

data class VegaQualityApproveNigeria(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()
)

data class VegaNigeriaCocoaWeightedAverageResponse(
    var weightedAvg: List<VegaNigeriaCocoaWeightedAverageDetails>,
    var imWerks: String? = ""
)

@Parcelize
data class VegaNigeriaCocoaWeightedAverageDetails(
    var atcod: String? = "",
    var atflv: String? = "",
    var atfor: String? = "",
    var atinn: String? = "",
    var atinp: String? = "",
    var atnam: String? = "",
    var atstd: String? = "",
    var atwrt: String? = "",
    var matnr: String? = "",
    var werks: String? = ""
) : Parcelable

data class VegaNigeriaCocoaMtntPostRequest(
    var batchNumber: String? = "",
    var delFlag: String? = "",
    var deliveryDetails: List<VegaNigeriaCocoaMtntDeliveryDetail>,
    var key: String? = "",
    var operatorName: String? = "",
    var weighmentType: String? = "",
    var plant: Plant
)
@Parcelize
data class VegaNigeriaCocoaDispatchLotsMerge(
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


data class VegaNigeriaCocoaMergedDeliveryPostResponse(
    var delFlag: String? = "",
    var binFormation: Boolean = false,
    var delivery: Boolean = false,
    var deliveryDetails: List<VegaNigeriaCocoaDispatchLotsMerge> = emptyList(),
    val key: String = getCurrentKey(),
    var pgi: Boolean = false,
    var pgiFlag: String? = "",
    var pickingBatchFlag: String? = "",
    var picking: Boolean = false,
    val plant: Plant,
    var message: String = ""
)

data class VegaNigeriaCocoaBinMergePostResponse(
    var message: String? = "",
    var errorCode: String? = "",
    var success: Boolean = false,
    var data: MergedData
)

data class MergedData(
    var type: String? = "",
    var message: String? = "",
    var batchNumber: String? = "",
    var totalNetWeight: String? = "",
    var responseData: String? = ""
)

data class VegaNigeriaCocoaWeightedAverageDeliveryDetail(
    var batchNumber: String? = "",
    var materialCode: String? = "",
    var qualityDetails: List<VegaNigeriaCocoaQualityDetails> = emptyList()
)

data class VegaNigeriaCocoaQualityDetails(
    var sapQCDesc: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
)

data class VegaNigeriaCocoaMtntDeliveryDetail(
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
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var isPgiFlag: Boolean = false,
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
    var endLotFlag: Boolean = false,
    var storageLossFlag: Boolean? = false,
    var msgList: List<String> = emptyList()
)

data class VegaNigeriaCocoaMtntPallet(
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

data class VegaNigeriaCocoaMtntWeighScalePallet(
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
