package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 3/31/2020.
 */

@Parcelize
@Entity(primaryKeys = ["grade", "deliveryNumber"])
data class MtnGrade(
    var grade: String = "",
    var deliveryNumber: String = "",
    var weight: String = ""
) : Parcelable
