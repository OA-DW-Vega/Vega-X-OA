package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model

import android.os.Parcelable
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InventoryBale(
    val baleCount: Long? = 0,
    var inventoryBaleListDTO: List<Bale>
) : Parcelable

data class GenericScanDetails(
    val lotlistDTO: IncomingLotDetails? = null,
    val baleDTO: Bale? = null,
    val scanType: Int = 0
)

@Parcelize
data class IncomingLotDetails(
    val lotNumber: String? = "",
    val truckNumber: String? = "",
    val containerNumber: String? = "",
    val createdTS: String? = "",
    val warehouseId: String? = ""
) : Parcelable
