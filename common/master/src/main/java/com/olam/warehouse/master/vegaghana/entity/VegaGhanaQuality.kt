package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity(primaryKeys = ["wbTempId", "nameChar"])
@Parcelize
data class VegaGhanaQuality(
    var position: Int = 0,
    var wbid: String = "",
    var wbTempId: String = "",
    var materialCode: String = "",
    var descrChar: String? = "",
    var nameChar: String = "",
    var entryObligatory: String? = "",
    var unitText: String? = "",
    var dataType: String? = "",
    var unitsOfMeasure: String? = "",
    var numberDigits: String? = "",
    var numberDecimals: String? = "",
    var numValFm: String? = "",
    var numValTo: String? = "",
    var currValFm: String? = "",
    var currValTo: String? = "",
    var valRelatn: String? = "",
    var timeStamp: String? = "",
    var qualityParameterValue: String? = "",
    var isSyncStatus: Boolean = false,
    var status: Int? = 0,
    var syncStatusMsg: String? = "",
    var preSampling: String? = "",
    var vegaMandatory: String? = "",
    var qualityParamLabel: String? = "",
    var formulaParam: String? = ""
) : Parcelable
