package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

@Parcelize
@Entity
data class IncomingLot(
    @PrimaryKey
    var lotNumber: String = "",
    var truckNumber: String = "",
    var containerNumber: String? = "",
    var createdTS: String = "",
    var plantId: String = PreferenceHelper.get(Constants.WERKS, ""),
    var scanType: Int = 0,
    var isSelected: Boolean = false
) : Parcelable
