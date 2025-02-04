package com.olam.warehouse.vegax.inventorycoffee.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.common.model.Warehouse
import com.olam.warehouse.master.user.model.CountryDetail
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class VegaCoffeeInventoryWarehouseModel(
    val kor: String = "",
    val averageKor: String = "",
    val averageKors: ArrayList<String>,
    val kors: ArrayList<String>,
    var storageLoc: List<CoffeeStorageLoc>,
    val warehouse: Warehouse,
    val weight: String = "",
    var isExpanded: Boolean = false
)

data class VegaCoffeeInventoryAndSyncModel(
    var inventories: List<VegaCoffeeInventoryWarehouseModel>,
    var syncStatus: List<CoffeeSyncStatus>
)

@Parcelize
data class CoffeeSyncStatus(
    var plant: String? = "",
    var status: String? = "",
    var updatedDateTime: String? = "",
    var updatedTimeInMS: String? = ""
) : Parcelable

data class CoffeeStorageLoc(
    val inventory: List<CoffeeInventory>,
    val kor: String? = "",
    val kors: ArrayList<String>,
    val averageKor: String? = null,
    val averageKors: ArrayList<String>,
    val materialDetails: List<MaterialDetails>,
    val origin: String? = "",
    val origins: ArrayList<String>,
    val warehouseLocation: CoffeeWarehouseLocation,
    val weight: String = ""
) : Serializable

data class CoffeeCountryDetail(
    val countryCode: String = "",
    val countryId: Int = 0,
    val countryName: String = ""
)


data class CoffeeInventoryQC(
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

data class FilterList(val value: String, var isSelected: Boolean = false)

data class Plant(
    val countryDetail: CountryDetail,
    val plantCode: String = "",
    val plantId: String = "",
    val plantName: String = ""
)

data class CoffeeWarehouse(
    val warehouseId: Int = 0,
    val warehouseName: String = ""
) : Serializable

data class CoffeeWarehouseLocation(
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


data class CoffeeInventory(
    var createdBy: String = "",
    var createdDateTime: String = "",
    var grnDate: String = "",
    var id: Int = 0,
    var inventoryQC: List<CoffeeInventoryQC>,
    var kor: String? = "",
    var lotId: String = "",
    var storageLocationCode: String? = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var origin: String? = "",
    var stockQty: String = "",
    var foreignMatter: String = "",
    var blackBeans: String = "",
    var brokenBeans: String = "",
    var updatedAt: String = "",
    var updatedBy: String = "",
    var moisture: String? = null,
    var warehouseLocation: CoffeeWarehouseLocation,
    var CI_MATIERE_ETRANGERE_CAFE: String = "",
    var CI_GRAINS_NOIRS_CAFE: String = "",
    var CI_BRISSURE_CAFE: String = ""
) : Serializable

data class MaterialDetails(
    val materialName: String = "",
    val materialCode: String = "",
    val totalWeight: String = "",
    val averageHumidity: String = ""
) : Serializable

data class VegaCoffeInventoryStocks(
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
    var warehouseLocation: CoffeeWarehouseLocation,
    var weight: String? = ""
) : Serializable

@Parcelize
data class Quality(
    var CI_MATIERE_ETRANGERE_CAFE: String = "",
    var CI_GRAINS_NOIRS_CAFE: String = "",
    var CI_BRISSURE_CAFE: String = ""
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
