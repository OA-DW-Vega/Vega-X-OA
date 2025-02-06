package com.olam.warehouse.vegax.ginningwarehouse.ui.data.model

import android.os.Parcelable
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeBaleStatus(
    var inventoryBaleDTO: Bale? = null,
    var istogrnPost: String? = "",
    var itransferPost: String? = ""
) : Parcelable
