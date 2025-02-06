package com.olam.warehouse.vegax.inventorynigeria.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
@Parcelize
data class VegaNigeriaInventoryLots(
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

data class VegaCocoaInventoryStocks(
    var batchNumber: String = "",
    var bkBez: String? = "",
    var bkLas: String? = "",
    var cinsm: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var materialText: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var materialQuality: MaterialQuality = MaterialQuality(),
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var warehouseLocation: WarehouseLocation,
    var weight: String? = ""
) : Serializable

data class InventoryLots(
    val createdBy: String = "",
    val createdDateTime: String = "",
    val grnDate: String = "",
    val id: Int = 0,
    val inventoryQC: List<InventoryQC>,
    val kor: String? = "",
    val lotId: String = "",
    val materialCode: String = "",
    var materialName: String = "",
    val origin: String? = "",
    val stockQty: String = "",
    val updatedAt: String = "",
    val updatedBy: String = "",
    val moisture: String? = null,
    val humidity: String? = "",
    val impurity: String? = "",
    val mould: String? = "",
    val admixture: String? = "",
    val unitOfMeasure: String? = "",
    val uom: String? = "",
    val warehouseLocation: WarehouseLocation
) : Serializable

data class MaterialDetails(
    val materialName: String = "",
    val materialCode: String = "",
    val totalWeight: String = "",
    val averageHumidity: String = "",
    val averageKor: String = "",
    val averageAdmixture: String = ""
) : Serializable


data class VegaInventoryWarehouseModel(
    var storageLoc: List<StorageLoc>,
    val warehouse: Warehouse,
    val weight: String = "",
    val admixtureRange: String = "",
    val korRange: String = "",
    var isExpanded: Boolean = false
)

data class StorageLoc(
    val admixtureRange: String = "",
    val inventory: List<InventoryLots>,
    val materialDetails: List<MaterialDetails>,
    val warehouseLocation: WarehouseLocation,
    val weight: String = ""
) : Serializable

data class Warehouse(
    val plant: Plant,
    val warehouseId: Int = 0,
    val warehouseName: String = ""
) : Serializable

data class Plant(
    val countryDetail: CountryDetail,
    val plantCode: String = "",
    val plantId: String = "",
    val plantName: String = ""
)

data class CountryDetail(
    val countryCode: String = "",
    val countryId: Int = 0,
    val countryName: String = ""
)

data class InventoryQC(
    val createdBy: String = "",
    val createdDateTime: String = "",
    val dataType: String = "",
    val entryObligatory: String = "",
    val id: Int = 0,
    val numberDecimals: String = "",
    val qcId: String = "",
    val qcName: String = "",
    val updatedAt: String = "",
    val updatedBy: String = "",
    val value: String = ""
) : Serializable

data class WarehouseLocation(
    val createdBy: String = "",
    val createdDateTime: String = "",
    val id: Int = 0,
    val procureLocationCode: String = "",
    val procureLocationName: String = "",
    val storageLocationType: String = "",
    val updatedBy: String = "",
    val updatedDateTime: String = "",
    val warehouse: Warehouse
) : Serializable

data class VegaInventoryAndSyncModel(
    var inventories: List<VegaInventoryWarehouseModel>,
    var syncStatus: List<SyncStatus>
)

@Parcelize
data class SyncStatus(
    var plant: String? = "",
    var status: String? = "",
    var updatedDateTime: String? = "",
    var updatedTimeInMS: String? = ""
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

@Parcelize
data class Quality(
    var ZNG_ADMIXTURE: String = "",
    var ZNG_CASHEW_KOR: String = "",
    var ZNG_GR_DATE: String = ""
//    var CI_MATIERE_ETRANGERE_CAFE: String = "",
//    var CI_GRAINS_NOIRS_CAFE: String = "",
//    var CI_BRISSURE_CAFE: String = ""
) : Parcelable

data class FilterList(val value: String, var isSelected: Boolean = false)
