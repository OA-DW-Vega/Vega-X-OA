package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */

@Parcelize
@Entity(primaryKeys = ["process", "rolekey", "materialCode", "cfgNo"])
data class VegaConfigDetails(
    var process: String = "",
    var keyFinder: String = "",
    var materialCode: String = "",
    var rolekey: String = "",
    @SerializedName("isApplicable")
    var applicable: String? = "",
    var plant: String? = "",
    var dynamicParam: String? = "",
    var value: String? = "",
    var value1: String? = "",
    var value2: String? = "",
    var cfgNo: String = "",
    var processStageName: String = "",
    @Ignore
    var processStage: ProcessStage? = ProcessStage(),
    @Ignore
    var sapClosureDaycount : String? = "0"
) : Parcelable

@Parcelize
class ProcessStage(
    var cfgNo: String = "",
    var fevor: String = "",
    var plant: String = "",
    var processName: String = ""
) : Parcelable
