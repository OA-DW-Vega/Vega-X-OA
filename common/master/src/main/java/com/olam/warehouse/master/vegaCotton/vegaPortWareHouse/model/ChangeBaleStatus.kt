package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import android.os.Parcelable
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeBaleStatus(
    var inventoryBaleDTO: PortBale? = null,
    var istogrnPost: String? = "",
    var itransferPost: String? = ""
) : Parcelable
