package com.olam.warehouse.vegax.processingnigeria.data.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VegaNigeriaProcessingQualityDetails(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<
            VegaNigeriaProcessingQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaNigeriaProcessingQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

data class VegaNigeriaProcessingRMINLotDetails(
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
    var shiftType: String? = ""
)

@Parcelize
data class VegaNigeriaProcessingStageDetails(
    var data: List<VegaNigeriaProcessingSubStageDetails> = emptyList()
) : Parcelable

@Parcelize
data class VegaNigeriaProcessingSubStageDetails(
    var materialCode: String? = "",
    var plant: String? = "",
    var processingStage: String? = "",
    var versionId: String? = ""
) : Parcelable
