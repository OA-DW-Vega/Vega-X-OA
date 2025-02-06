package com.olam.warehouse.vegax.inventorynicaragua.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.common.model.Warehouse
import com.olam.warehouse.master.user.model.CountryDetail
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class VegaNicInventoryWarehouseModel(
        var storageLoc: List<NicStorageLoc>,
        val warehouse: Warehouse,
        val weight: String = "",
        var isExpanded: Boolean = false
)

data class VegaNicInventoryAndSyncModel(
        var inventories: List<VegaNicInventoryWarehouseModel>,
        var syncStatus: List<NicSyncStatus>,
        var materialWiseSyncStatus: List<NicSyncStatus>
)

@Parcelize
data class NicSyncStatus(
        var plant: String? = "",
        var status: String? = "",
        var updatedDateTime: String? = "",
        var updatedTimeInMS: String? = "",
        var materialCode: String? = ""
) : Parcelable

@Parcelize
data class NicStorageLoc(
        var procureLocationCode: String = "",
        var procureLocationName: String = "",
        var inventory: List<NicInventory>,
        var materialDetails: List<MaterialDetails>,
        var warehouseLocation: NicWarehouseLocation,
        var weight: String = "",
        var isChecked: Boolean = false
) : Serializable, Parcelable

data class NicCountryDetail(
        val countryCode: String = "",
        val countryId: Int = 0,
        val countryName: String = ""
)

@Parcelize
data class NicInventoryQC(
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
) : Serializable, Parcelable

data class FilterList(var value: String = "", var isSelected: Boolean = false)

data class Plant(
        val countryDetail: CountryDetail,
        val plantCode: String = "",
        val plantId: String = "",
        val plantName: String = ""
)

data class NicWarehouse(
        val warehouseId: Int = 0,
        val warehouseName: String = ""
) : Serializable

@Parcelize
data class NicWarehouseLocation(
        val createdBy: String = "",
        val createdDateTime: String = "",
        val id: Int = 0,
        val procureLocationCode: String = "",
        val procureLocationName: String = "",
        val storageLocationType: String = "",
        val updatedBy: String = "",
        val updatedDateTime: String = "",
        val warehouse: Warehouse
) : Serializable, Parcelable

@Parcelize
data class NicInventory(
    var createdBy: String = "",
    var createdDateTime: String = "",
    var grnDate: String = "",
    var id: Int = 0,
    var inventoryQC: List<NicInventoryQC>? = emptyList(),
    var lotId: String = "",
    var qualityGrade: String? = "",
    var gradeDesc: String? = "",
    var certification: String? = "",
    var storageLocationCode: String? = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var openQuantity: String? = "",
    var stockQty: String? = "",
    var uom: String? = "",
    var vendorCode: String = "",
    var qualityParameters: List<QualityParams>? = emptyList(),
    var warehouseLocation: NicWarehouseLocation,
    var isChecked: Boolean = false,
    var CI_MATIERE_ETRANGERE_CAFE: String = "",
    var CI_GRAINS_NOIRS_CAFE: String = "",
    var CI_BRISSURE_CAFE: String = ""
) : Serializable, Parcelable

@Parcelize
data class MaterialDetails(
    var materialName: String = "",
    var materialCode: String = "",
    var qualityGrade: String = "",
    var certification: String? = "",
    var totalWeight: String = "",
    var uom: String = "",
    var averageHumidity: String = ""
) : Serializable, Parcelable


data class VegaNicInventoryStocks(
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
    var warehouseLocation: NicWarehouseLocation,
    var weight: String? = ""
) : Serializable

@Parcelize
data class Quality(
    var NIPOSITI: String = "",
    var NIFG0014: String = "",
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
