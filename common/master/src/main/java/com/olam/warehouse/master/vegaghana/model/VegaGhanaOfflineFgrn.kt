package com.olam.warehouse.master.vegaghana.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.common.data.domain.model.messageDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.presentation.enums.Status


class VegaGhanaOfflineFgrn {
    @Embedded
    lateinit var fgrnData: VegaGhanaOfflineFgrnData

    @Relation(
        parentColumn = "fgrnTempId",
        entityColumn = "fgrnTempId",
        entity = VegaGhanaOfflineFgrnProcessLotDetails::class
    )
    var fgrnLot: List<VegaGhanaOfflineFgrnProcessLotDetails> = emptyList()
}

data class VegaGhanaOfflineProcessingFgrnPost(
    var key: String? = "",
    var plant: Plant? = null,
    var processingStage: String? = "",
    var outputMaterialCode: String? = "",
    var processingLotDtls: List<GhanaOfflineProcessingFgrnLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var shiftType: String? = "",
    var operatorName: String? = "",
    var versionId: String? = ""
)

data class GhanaOfflineProcessingFgrnLotDetails(
    var bagCount: String? = "",
    var batchNumber: String? = "",
    var confText: String? = "",
    var deliveryItem: String? = "",
    var materialCode: String? = "",
    var menge: String? = "",
    var movementType: String? = "",
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var bagType: String? = "",
    var year: String? = "",
    var processOrderNum: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var xchpf: String? = "",
    var huno: String? = "", // bag uom
    var huwt: String? = "", // bag tare weight
    var huno2: String? = "", //pallet uom
    var huwt2: String? = "", // pallet tare weight
    var nohu1: String? = "", // no of bags
    var nohu2: String? = "", // no of pallet
    var bagMaterialCode: String? = "",
    var endLotFlag: Boolean = false,
    var vendorCode: String? = "",
    var storageLossFlag: Boolean = false,
    var bagList: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList()

)

data class VegaGhanaOfflineProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var encodedImageContent: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

data class VegaGhanaMtntDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaGhanaMtntDeliveryDetail> = emptyList(),
    var textNavListValues: List<TextNavListValues> = emptyList(),
    val weighmentType: String,
    var vehicleNumber: String? = "",
    var driverLicenseNumber: String? = "",
    val vehicleType: String = "",
    val contactNumber: String = "",
    val driverName: String = "",
    val purchaseOrg: String = ""
)

data class TextNavListValues(
    var textId: String? = "",
    var textValue: String? = ""
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
    var materialName: String? = "",
    var msg: String? = "",
    var netWeight: String? = "",
    var plantId: String? = "",
    var soWeight: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    var departurePoint: String? = "",
    var remarks: String? = "",
    var salesItem: String? = "",
    var salesOrderNum: String? = "",
    var startTime: String? = "",
    var storageLocationCode: String? = "",
    var purchaseQuantity: String? = "",
    var routeLocCode: String? = "",
    var textId: String? = "",
    var textValue: String? = "",
    var shipmentNumber: String? = "",
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
    var driverLicenseNumber: String? = "",
    var vehicleNumber: String? = "",
    var fromVendorCode: String? = "",
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var isPgiFlag: Boolean = false,
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
    var endLotFlag: Boolean = false,
    var storageLossFlag: Boolean? = false,
    var msgList: List<String> = emptyList()
)

data class VegaGhanaProcessingCreatePoReq(
    var cfgNo: String = "",
    var processingStage: String? = "",
    var batchNumber: String = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<VegaGhanaProcessingRMINLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var versionId: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var date: String? = "",
    var syncStatusMsg: String? = "",
    var isProgress: Boolean = false,
    var fgrn: Boolean? = false,
    var atSourceVal: String? = "",
)

data class VegaGhanaProcessingRMINLotDetails(
    var bagCount: String? = "",
    var batchNumber: String? = "",
    var confText: String? = "",
    var deliveryItem: String? = "",
    var materialCode: String? = "",
    var menge: String? = "",
    var movementType: String? = "",
    var netWeight: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var processOrderNum: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var xchpf: String? = "",
    var resource: String? = "",
    var remarks: String? = "",
    var shiftType: String? = "",
    var bagList: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList()
)


