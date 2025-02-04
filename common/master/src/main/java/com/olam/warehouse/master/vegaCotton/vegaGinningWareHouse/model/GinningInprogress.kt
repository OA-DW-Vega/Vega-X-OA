package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by SangiliPandian C on 16-03-2020.
 */
@Parcelize
data class GinningInprogress(
    @PrimaryKey
    var lotNumber: String,
    var plantId: String = PreferenceHelper.get(Constants.WERKS, ""),
    var grossWeight: Double? = 0.0,
    var netWeight: Double? = 0.0,
    var createdDate: String? = "",
    var status: String? = "",
    var userName: String? = "",
    var bales: List<Bale>? = emptyList()
) : Parcelable
