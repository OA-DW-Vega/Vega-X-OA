package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
class VegaProcessingStage(
    var fevor: String = "",
    var plant: String? = "",
    var auart: String? = "",
    @PrimaryKey
    var cfgNo: String = "",
    var processName: String? = "",
    var blendingType: Boolean? = false,
    var process: String? = ""
) : Parcelable
