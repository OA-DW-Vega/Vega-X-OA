package com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaIndiaCoffeeQualityDetails(
    var grnDate: String = "",
    var id: Int = 0,
    var kor: String? = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var storageLocationCode: String = "",
    var unitOfMeasure: String? = "",
    var weight: String = "",
    var origin: String? = "",
    var stockQty: String = "",
    var materialQuality: IndiaCoffeeMaterialQuality = IndiaCoffeeMaterialQuality(),
    var moisture: String? = null
) : Parcelable

data class IndiaCoffeeGrnFilterList(val value: String, var isSelected: Boolean = false)

data class VegaIndiaCoffeeInventoryLotHead(
    var syncStatus: List<IndiaCoffeeSyncStatus> = emptyList(),
    var stocks: List<VegaIndiaCoffeeQualityDetails> = emptyList()
)

@Parcelize
data class IndiaCoffeeSyncStatus(
    var plant: String? = "",
    var status: String? = "",
    var updatedDateTime: String? = "",
    var updatedTimeInMS: String? = ""
) : Parcelable

@Parcelize
data class IndiaCoffeeQuality(
    var B_MOIST: String = "",
    var B_FFA1: String = "",
    var B_FAT1: String = "",
    var B_BEANCOUNT: String = ""
) : Parcelable

@Parcelize
data class IndiaCoffeeMaterialQuality(
    var charg: String = "",
    var materialNumber: String = "",
    var qualityParams: IndiaCoffeeQuality = IndiaCoffeeQuality(),
    var qualityParameters: List<IndiaCoffeeQualityParams> = emptyList()
) : Parcelable

@Parcelize
data class IndiaCoffeeQualityParams(
    var sapQCName: String = "",
    var satNam: String = ""
) : Parcelable

data class IndiaCoffeeFilterList(val value: String, var isSelected: Boolean = false)

@Parcelize
data class VegaIndiaCoffeeGRNQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

@Parcelize
data class VegaIndiaCoffeeGRNQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaIndiaCoffeeGRNQualityParams> = emptyList()
) : Parcelable
