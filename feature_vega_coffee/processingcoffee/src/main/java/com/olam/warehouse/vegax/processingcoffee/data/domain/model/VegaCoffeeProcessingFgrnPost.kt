package com.olam.warehouse.vegax.processingcoffee.data.domain.model

import com.olam.warehouse.master.common.data.domain.model.messageDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.presentation.enums.Status


data class CoffeeFilterList(val value: String, var isSelected: Boolean = false)

class VegaCoffeeProcessingOrderReq(
    var key: String? = "",
    var auart: String? = "",
    var cfgNo: String? = "",
    var fevor: String? = "",
    var plant: Plant = Plant()
)

data class VegaCoffeeRminProcessingPost(
    var processingStage: String? = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<VegaCoffeeProcessingRMINLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var versionId: String? = "",
    var remarks: String? = "",
    var shiftType: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var xchpf: String? = "",
    var bwart: String? = ""
)

data class VegaCoffeeProcessingRminResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

data class VegaCoffeeProcessingCreatePoReq(
    var cfgNo: String = "",
    var processingStage: String? = "",
    var batchNumber: String = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<VegaCoffeeProcessingRMINLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var versionId: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var date: String? = "",
    var syncStatusMsg: String? = "",
    var isProgress: Boolean = false
)


data class VegaCoffeeFgrnBagMaterialWithId(
    var bagMaterialCode: String = "",
    var fgrnIdMaterial: String = "",
    var batchNumber: String = ""
)

data class VegaCoffeeProcessingFgrnPost(
    var processingStage: String? = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<CoffeeProcessingFgrnLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var shiftType: String? = "",
    var operatorName: String? = "",
    var versionId: String? = ""
)

data class CoffeeProcessingFgrnLotDetails(
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

data class VegaCocoaProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var encodedImageContent: String? = "",
    var messages: List<messageDetails>? = emptyList()
)
