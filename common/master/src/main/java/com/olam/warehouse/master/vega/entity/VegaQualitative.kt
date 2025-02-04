package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/10/2020.
 */
@Entity(primaryKeys = ["materialCode", "nameChar", "charValue"])
@Parcelize
data class VegaQualitative(
    var charValue: String = "",
    var materialCode: String = "",
    var nameChar: String = "",
    var descValue: String = ""
) : Parcelable
