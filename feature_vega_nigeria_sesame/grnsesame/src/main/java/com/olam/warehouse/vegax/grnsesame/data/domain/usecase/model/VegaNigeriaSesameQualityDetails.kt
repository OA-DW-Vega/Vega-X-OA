package com.olam.warehouse.vegax.grnsesame.data.domain.usecase.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
@Parcelize
data class VegaNigeriaSesameQualityDetails(
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

data class NigeriaGrnFilterList(val value: String, var isSelected: Boolean = false)

data class VegaNigeriaInventoryLotHead(
    var syncStatus: List<SyncStatus> = emptyList(),
    var stocks: List<VegaNigeriaSesameQualityDetails> = emptyList()
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
data class VegaGRNSesameQualityParams(
    var qualityParameterId: String? = "",
    var qualityParameterName: String? = "",
    var qualityParameterType: String? = "",
    var maxValue: String? = "",
    var minValue: String? = "",
    var sapQCName: String? = "",
    var satNam: String? = ""
) : Parcelable

@Parcelize
data class VegaGRNSesameQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaGRNSesameQualityParams> = emptyList()
) : Parcelable
