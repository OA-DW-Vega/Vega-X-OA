package com.olam.warehouse.vegax.processingindo.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaProcessingIndoQualityDetails(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaProcessingIndoQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaProcessingIndoQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

data class VegaProcessingIndoRMINLotDetails(
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
