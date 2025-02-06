package com.olam.warehouse.master.common.model

import androidx.room.Embedded
import androidx.room.Ignore

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class QualityParameter(
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
    var appFormula: String? = "",
    var parameterUsage: String? = "",
    @Embedded
    @Ignore
    var qualitative: List<Qualitative>? = emptyList(),
    var mandatory: Int? = 0,
    var preSampling: String? = "",
    var vegaMandatory: String? = "",
    var vegaValueMandatory: String? = "",
    var qualityParamLabel: String? = "",
    var formulaParam: String? = "",
    var doMandatory: String? = ""
)
