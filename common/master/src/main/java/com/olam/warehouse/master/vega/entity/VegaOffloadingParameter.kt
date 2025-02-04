package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.olam.warehouse.presentation.enums.Status
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 2/1/2020.
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = VegaOffloadingTrucks::class,
        parentColumns = arrayOf("wbTempId"),
        childColumns = arrayOf("wbTempId"),
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["wbid", "nameChar"]
)
@Parcelize
data class VegaOffloadingParameter(
    var position: Int = 0,
    var wbid: String = "",
    @ColumnInfo(index = true)
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
    var status: Status = Status.SYNC_PENDING,
    var pre_sampling: String? = "",
    var vaga_mandatory: String? = "",
    var quality_param_label: String? = "",
    var formulaParam: String? = ""
) : Parcelable
