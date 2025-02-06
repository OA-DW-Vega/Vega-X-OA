package com.olam.warehouse.master.common.model

import androidx.room.ColumnInfo
import com.olam.warehouse.presentation.enums.Status

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class Quality(
    var position: Int = 0,
    var wbid: String = "",
    @ColumnInfo(index = true)
    var wbTempId: String = "",
    var matnr: String = "",
    var descrChar: String? = "",
    var nameChar: String = "",
    var entryObligatory: String? = "",
    var unitText: String? = "",
    var dataType: String? = "",
    var unit: String? = "",
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
    var status: Status = Status.SYNC_PENDING
)
