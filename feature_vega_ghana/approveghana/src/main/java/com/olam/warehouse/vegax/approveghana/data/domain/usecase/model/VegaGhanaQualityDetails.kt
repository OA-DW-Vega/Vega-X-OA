package com.olam.warehouse.vegax.approveghana.data.domain.usecase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
@Parcelize
data class VegaGhanaQualityDetails(
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
    var materialQuality: MaterialQuality = MaterialQuality(),
    var moisture: String? = null
) : Parcelable

data class GhanaGrnFilterList(val value: String, var isSelected: Boolean = false)

data class VegaGhanaInventoryLotHead(
    var syncStatus: List<SyncStatus> = emptyList(),
    var stocks: List<VegaGhanaQualityDetails> = emptyList()
)

@Parcelize
data class SyncStatus(
    var plant: String? = "",
    var status: String? = "",
    var updatedDateTime: String? = "",
    var updatedTimeInMS: String? = ""
) : Parcelable

@Parcelize
data class Quality(
    var B_MOIST: String = "",
    var B_FFA1: String = "",
    var B_FAT1: String = "",
    var B_BEANCOUNT: String = ""
) : Parcelable

@Parcelize
data class MaterialQuality(
    var charg: String = "",
    var materialNumber: String = "",
    var qualityParams: Quality = Quality(),
    var qualityParameters: List<QualityParams> = emptyList()
) : Parcelable

@Parcelize
data class QualityParams(
    var sapQCName: String = "",
    var satNam: String = ""
) : Parcelable

data class FilterList(val value: String, var isSelected: Boolean = false)

@Parcelize
data class VegaGRNGhanaQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = "",
    var descChar: String? = ""
) : Parcelable

@Parcelize
data class VegaGRNGhanaQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaGRNGhanaQualityParams> = emptyList()
) : Parcelable
