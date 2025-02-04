package com.olam.warehouse.ginning.data.model

import android.os.Parcelable
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeBaleStatus(
    var inventoryBaleDTO: Bale? = null,
    var istogrnPost: String? = "",
    var itransferPost: String? = ""
) : Parcelable
