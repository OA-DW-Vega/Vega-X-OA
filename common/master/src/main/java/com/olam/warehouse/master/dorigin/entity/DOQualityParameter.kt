package com.olam.warehouse.master.dorigin.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.common.utils.getCurrentKey

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Entity(primaryKeys = ["materialCode", "nameChar"])
data class DOQualityParameter(
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
    var singleValue: String? = "",
	var appFormula: String? ="",
    var parameterUsage: String? = "",
    @Embedded
    @Ignore
    var qualitative: List<DOQualitative>? = emptyList(),
    var mandatory: Int? = 0,
    var doMandatory: String? = "",
    var formulaParam: String? = "",
    var currentKey: String? = getCurrentKey()
)
