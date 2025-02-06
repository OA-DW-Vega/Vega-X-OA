package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

@Parcelize
data class VegaCocoaLotQualityInspectionLots(
    var inspectionLotNum: String = "",
    var chargeNum: String = "",
    var materialName: String = "",
    var materialCode: String = "",
    var weight: String = "",
    var unitOfMeasure: String = "",
    var inspectionDate: String = "",
    var date: String = "",
    var plantId: String = ""
) : Parcelable

@Parcelize
data class VegaCocoaLotQualityInspectionLotDetails(
    var inspectionLotNum: String = "",
    var materialCode: String = "",
    var plantId: String = "",
    var inspectionOperationNum: String = "",
    var usageDecision: Boolean = false,
    var qualityParameters: List<VegaCocoaLotQualityInspectionLotParams> = emptyList()
) : Parcelable

@Parcelize
data class VegaCocoaLotQualityInspectionLotParams(
    var charDesc: String = "",
    var formula: String = "",
    var meansUnit: String = "",
    var Insplot: String = "",
    var Inspoper: String = "",
    var Inspchar: String = "",
    var Closed: String = "",
    var MeanValue: String = "",
    var codeValue: String = "",
    var qualityParameterValue: String? = "",
    var code: String = "",
    var codeGroup: String = "",
    var Code1: String = "",
    var CodeGrp1: String = "",
    var mandatory: Int? = 0,
    var quantitative: LotQualityParamsQuantitative = LotQualityParamsQuantitative(),
    var qualitative: List<LotQualityParamsQualitative> = emptyList()
) : Parcelable

@Parcelize
data class LotQualityParamsQualitative(
    var codeGroup: String = "",
    var code: String = "",
    var codeTxt: String = "",
    var codeGroupTxt: String = "",
    var codeValue: String = ""
) : Parcelable

@Parcelize
data class LotQualityParamsQuantitative(
    var lowerLimit: String = "",
    var upperLimit: String = ""
) : Parcelable
