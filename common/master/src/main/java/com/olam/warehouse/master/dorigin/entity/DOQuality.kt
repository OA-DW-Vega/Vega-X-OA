package com.olam.warehouse.master.dorigin.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.enums.Status
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Parcelize
@Entity(
    foreignKeys = [ForeignKey(
        entity = DOQualityWBDetails::class,
        parentColumns = arrayOf("wbTempId"),
        childColumns = arrayOf("wbTempId"),
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["wbid", "nameChar"]
)
data class DOQuality(
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
    var doMandatory: String? = "",
    var formulaParam: String? = "",
    var currentKey: String? = getCurrentKey()
) : Parcelable
