package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InventoryBale(
    val baleCount: Long? = 0,
    var inventoryBaleListDTO: List<PortBale>
) : Parcelable
