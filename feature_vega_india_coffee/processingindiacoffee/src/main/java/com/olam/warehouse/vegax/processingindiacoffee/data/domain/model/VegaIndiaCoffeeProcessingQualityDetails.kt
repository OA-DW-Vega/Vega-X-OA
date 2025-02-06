package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaIndiaCoffeeProcessingQualityDetails(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaIndiaCoffeeProcessingQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaIndiaCoffeeProcessingQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

data class VegaIndiaCoffeeProcessingRMINLotDetails(
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
    var startTime: String? = "",
    var postingDate: String? = ""
)
