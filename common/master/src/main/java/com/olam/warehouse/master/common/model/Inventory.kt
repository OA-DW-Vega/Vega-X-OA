package com.olam.warehouse.master.common.modal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class InventoryResponse(

    val inventory: List<Inventory>
)


data class OfflineInventory(

    val inventory: List<Inventory>
)

@Parcelize
data class Inventory(

    val id: String? = "",
    val warehouseLocation: WarehouseLocation? = null,
    val materialCode: String? = "",
    val lotId: String? = "",
    val stockQty: String? = "",
    val grnDate: String? = "",
    val createdDateTime: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = "",
    val inventoryQC: List<InventoryQC>? = null,
    var vendorCode: String? = "",
    var uom: String? = "",
    var qualityGrade: String? = "",
    var gradeDesc: String? = "",
    val certification: String? = ""
) : Parcelable

@Parcelize
data class WarehouseLocation(

    val id: String? = "",
    val warehouse: Warehouse? = null,
    val procureLocationCode: String? = "",
    val procureLocationName: String? = "",
    val createdDateTime: String? = "",
    val createdBy: String? = "",
    val updatedDateTime: String? = "",
    val updatedBy: String? = ""
) : Parcelable

@Parcelize
data class Warehouse(

    val warehouseId: String? = "",
    val warehouseName: String? = "",
    val plant: Plant? = null
) : Parcelable

@Parcelize
data class Plant(

    val plantId: String? = "",
    val plantName: String? = "",
    val countryDetail: CountryDetail? = null,
    val plantCode: String? = ""
) : Parcelable

@Parcelize
data class CountryDetail(

    val countryId: String? = "",
    val countryName: String? = "",
    val countryCode: String? = "",
    val currency: String? = ""
) : Parcelable

@Parcelize
data class InventoryQC(

    val id: String? = "",
    val qcId: String? = "",
    val qcName: String? = "",
    val dataType: String? = "",
    val numberDecimals: String? = "",
    val entryObligatory: String? = "",
    val value: String? = "",
    val createdDateTime: String? = "",
    val createdBy: String? = "",
    val updatedAt: String? = "",
    val updatedBy: String? = ""
) : Parcelable
