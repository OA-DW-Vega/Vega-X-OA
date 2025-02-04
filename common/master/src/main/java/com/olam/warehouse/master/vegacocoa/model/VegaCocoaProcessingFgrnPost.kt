package com.olam.warehouse.master.vegacocoa.model

import android.os.Parcelable
import com.olam.warehouse.master.common.data.domain.model.messageDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import kotlinx.android.parcel.Parcelize

data class VegaCocoaProcessingFgrnPost(
    var processingStage: String? = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<CocoaProcessingFgrnLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var shiftType: String? = "",
    var operatorName: String? = "",
    var versionId: String? = ""
)

data class CocoaProcessingFgrnLotDetails(
    var bagCount: String? = "",
    var batchNumber: String? = "",
    var sequence: String? = "",
    var confText: String? = "",
    var deliveryItem: String? = "",
    var materialCode: String? = "",
    var menge: String? = "",
    var movementType: String? = "",
    var netWeight: String? = "",
    var netWeight1: String? = "",
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
    var unitsOfMeasure1: String? = "",
    var xchpf: String? = "",
    var huno: String? = "", // bag uom
    var huwt: String? = "", // bag tare weight
    var huno2: String? = "", //pallet uom
    var huwt2: String? = "", // pallet tare weight
    var nohu1: String? = "", // no of bags
    var nohu2: String? = "", // no of pallet
    var bagMaterialCode: String? = "",
    var bagList: List<VegaCocoaFgrnGradesMatrialWeights>? = emptyList(),
    var qualityDetails: List<VegaNicaraguaFgrnQuality>? = emptyList()

)

data class VegaCocoaProcessingFgrnResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var encodedImageContent: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

@Parcelize
data class VegaNicaraguaFgrnQuality(
    var materialCode: String = "",
    var plantId: String = "",
    var storageLocationCode: String = "",
    var nameCharValue: String = "",
    var descrCharValue: String = "",
    var qualityParameterValue: String = "",
    var batchNumber: String = ""
) : Parcelable






