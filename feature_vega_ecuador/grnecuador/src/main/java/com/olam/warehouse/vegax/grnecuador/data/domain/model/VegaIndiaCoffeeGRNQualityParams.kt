package com.olam.warehouse.vegax.grnecuador.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaEcuadorGRNQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

@Parcelize
data class VegaEcuadorGRNQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaEcuadorGRNQualityParams> = emptyList()
) : Parcelable

data class VegaEcuadorCocoaGrnPostData(
    var batchNumber: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var storageLocationCode: String? = "",
    var price: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",
    var materialCode: String? = "",
    var item: String? = "",
    var currency: String? = "",
    var purchaseDocNum: String? = ""
)
