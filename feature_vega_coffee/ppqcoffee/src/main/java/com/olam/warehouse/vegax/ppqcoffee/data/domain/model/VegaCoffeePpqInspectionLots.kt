package com.olam.warehouse.vegax.ppqcoffee.data.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
@Parcelize
data class VegaCoffeePpqInspectionLots(
    var inspectionLotNum: String = "",
    var chargeNum: String = "",
    var materialName: String = "",
    var materialCode: String = "",
    var weight: String = "",
    var unitOfMeasure: String = "",
    var inspectionDate: String = "",
    var date: String = ""
) : Parcelable

@Parcelize
data class VegaCoffeePpqInspectionLotDetails(
    var inspectionLotNum: String = "",
    var inspectionOperationNum: String = "",
    var usageDecision: Boolean = false,
    var qualityParameters: List<VegaCoffeePpqInspectionLotParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaCoffeePpqInspectionLotParams(
    var charDesc: String = "",
    var formula: String = "",
    var meansUnit: String = "",
    var Insplot: String = "",
    var Inspoper: String = "",
    var Inspchar: String = "",
    var Closed: String = "",
    var MeanValue: String = "",
    var qualityParameterValue: String? = "",
    var code: String = "",
    var codeGroup: String = "",
    var Code1: String = "",
    var CodeGrp1: String = "",
    var mandatory: Int? = 0,
    var quantitative: PpqParamsQuantitative = PpqParamsQuantitative(),
    var qualitative: List<PpqParamsQualitative> = emptyList()
) : Parcelable

@Parcelize
data class PpqParamsQualitative(
    var codeGroup: String = "",
    var code: String = "",
    var codeTxt: String = "",
    var codeGroupTxt: String = "",
    var codeValue: String = ""
) : Parcelable

@Parcelize
data class PpqParamsQuantitative(
    var lowerLimit: String = "",
    var upperLimit: String = ""
) : Parcelable
