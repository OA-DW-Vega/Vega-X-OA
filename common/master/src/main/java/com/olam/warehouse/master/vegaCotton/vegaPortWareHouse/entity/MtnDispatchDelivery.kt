package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 3/31/2020.
 */

@Entity
@Parcelize
data class MtnDispatchDelivery(
    @PrimaryKey
    var deliveryNumber: String = "",
    var truckNumber: String? = "",
    var containerNumber: String? = "",
    var maxBaleAllowed: Int? = 0,
    var maxWeightAllowed: Double? = 0.0,
    @Embedded
    @Ignore
    var gradeDTO: List<MtnGrade> = emptyList(),
    @Embedded
    @Ignore
    var baleDTO: List<MtnBale> = emptyList(),
    var userName: String? = "",
    var isReadyForDispatch: Boolean? = false,
    var message: String? = "",
    var isErrorStatus: Boolean = true,
    var status: Int? = 1
) : Parcelable
