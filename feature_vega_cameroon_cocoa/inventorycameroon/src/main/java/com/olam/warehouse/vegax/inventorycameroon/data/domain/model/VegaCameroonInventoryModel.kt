package com.olam.warehouse.vegax.inventorycameroon.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
data class VegaInventoryWarehouseModel(
    var storageLoc: List<StorageLoc>,
    val warehouse: Warehouse,
    val weight: String = "",
    val beanMoistureRange: String = "",
    var isExpanded: Boolean = false
)

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

data class StorageLoc(
    val beanMoistureRange: String = "",
    val inventory: List<InventoryLots>,
    val materialDetails: List<MaterialDetails>,
    val warehouseLocation: WarehouseLocation,
    val weight: String = ""
) : Serializable

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

data class FilterList(val value: String, var isSelected: Boolean = false)

data class Plant(
    val countryDetail: CountryDetail,
    val plantCode: String = "",
    val plantId: String = "",
    val plantName: String = ""
)

data class Warehouse(
    val warehouseId: Int = 0,
    val warehouseName: String = ""
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
    val beanMoisture: String? = "",
    val impurity: String? = "",
    val mould: String? = "",
    val smoky: String? = "",
    val waste: String? = "",
    val warehouseLocation: WarehouseLocation
) : Serializable

data class MaterialDetails(
    val materialName: String = "",
    val materialCode: String = "",
    val totalWeight: String = "",
    val averageBeanMoisture: String = ""
) : Serializable
