package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */
@Parcelize
@Entity(primaryKeys = ["grade", "deliveryNumber"])
data class Grade(
    var grade: String = "",
    var deliveryNumber: String = "",
    var weight: String = ""
) : Parcelable
