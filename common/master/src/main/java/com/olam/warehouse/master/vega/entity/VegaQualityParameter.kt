package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.enums.Status
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity(primaryKeys = ["materialCode", "nameChar"])
@Parcelize
data class VegaQualityParameter(
    var position: Int = 0,
    var priorityOrder: String? = "",
    var wbid: String = "",
    var wbTempId: String? = "",
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
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var singleValue: String? = "",
    @Embedded
    @Ignore
    var qualitative: List<VegaQualitative>? = emptyList(),
    var mandatory: Int? = 0,
    var preSampling: String? = "",
    var vegaMandatory: String? = "",
    var vegaValueMandatory: String? = "",
    var qualityParamLabel: String? = "",
    var formulaParam: String? = "",
    var isEditable: Boolean? = true
) : Parcelable
