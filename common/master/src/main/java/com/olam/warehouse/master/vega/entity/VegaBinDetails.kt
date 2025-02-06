package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */

@Entity
@Parcelize
data class VegaBinDetails(
    @PrimaryKey
    var binLocationCode: String = "",
    var procureLocationCode: String? = "",
    var binLocationName: String? = ""
) : Parcelable
