package com.olam.warehouse.vegax.qualitynigeria.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
const val APPROVE_BATCH_DETAILS = "batchDetails"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val APPROVE_JUTE_BAG_DETAILS = "approve_jute_bag"
const val WEIGHMENT_TYPE = "weighment_type"
const val WEIGHBRIDGE_LIST = "weigh_bridge_list"
const val QUALITY_OFFLINE = "qty_offline"
const val QUALITY_OFFLINE_LIST = "qty_offline_list"
const val MTNR = "MTN-R"
const val SUPPLIER = "Supplier"
const val PARAMS_LIST = "params_list"
const val SUMMARY_LIST = "summary_list"
const val WEIGHBRIDGE_LIST_TYPE = "weigh_bridge_list_type"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
const val IS_PARAMS_VALUE = "is_params_values"
const val BATCH_NO = "batch_no"
const val MATERIAL_NO = "material_no"
const val WEIGHSCALE = "weigh_scale"
const val FNQUALITY = ""
const val FNQUALITY_THRESHOLD = "X"
const val FNREJECT = "D"
const val WAREHOUSE = "Warehouse"
const val NET_WEIGHT = "net_weight"
const val TAR_WEIGHT = "tar_weight"
const val CHALLAN = "challan"
const val WB_ID = "wb_id"
const val FINAL_APPROVAL = "final_approval"
const val STORAGELOCATION_CODE = "storageLocationCode"
const val FLAG = "flag"
const val AVG_BAG_WEIGHT = "avg_bag_weight"
const val BATCH_NUMBER = "batch_number"
const val GROSS_WEIGHT = "gross_weight"
const val B_MOULD4 = "B_MOULD4"
const val NG_ADMIX = "NG_ADMIX"
const val ZNGCOCOA_ACTBW = "ZNGCOCOA_ACTBW"
const val B_DCTBW1 = "B_DCTBW1"
const val B_BEANCOUNT = "B_BEANCOUNT"
const val B_SL = "B_SL"
const val B_MOIST = "B_MOIST"
const val MOULD_VALUE = "mould_value"
const val ADD_MIXTURE = "add_Mixture"
const val BEAN_WT_GRAM = "bean_weight_gram"
const val BEAN_COUNT = "bean_count"
const val SLATY = "slaty"
const val PLANTID = "plantid"
const val MOISTURE = "moisture"
const val JUTE_BAG_MATERIAL_CODE = "000000200000004144"
const val ACTUAL_BEAN_WEIGHT = "Actual Bean Weight"

fun prepareData(quality1: List<VegaQualityWithQualitative>): LiveData<List<VegaQualityParamsWithQualitative>> {
    val qualityParamList = arrayListOf<VegaQualityParamsWithQualitative>()
    val qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.quality.position }
            qualitySortedBy.forEach { quality ->
                val qualityParamsQualitative = VegaQualityParamsWithQualitative()
                val qualityParams = VegaQualityParameter()
                qualityParams.wbid = quality.quality.wbid
                qualityParams.wbTempId = quality.quality.wbTempId
                qualityParams.materialCode = quality.quality.materialCode
                qualityParams.descrChar = quality.quality.descrChar
                qualityParams.nameChar = quality.quality.nameChar
                qualityParams.entryObligatory = quality.quality.entryObligatory
                qualityParams.unitText = quality.quality.unitText
                qualityParams.dataType = quality.quality.dataType
                qualityParams.unitsOfMeasure = quality.quality.unitsOfMeasure
                qualityParams.numberDigits = quality.quality.numberDigits
                qualityParams.numberDecimals = quality.quality.numberDecimals
                qualityParams.numValFm = quality.quality.numValFm
                qualityParams.numValTo = quality.quality.numValTo
                qualityParams.currValFm = quality.quality.currValFm
                qualityParams.currValTo = quality.quality.currValTo
                qualityParams.valRelatn = quality.quality.valRelatn
                qualityParams.timeStamp = quality.quality.timeStamp
                qualityParams.preSampling = quality.quality.preSampling
                qualityParams.vegaMandatory = quality.quality.vegaMandatory
                qualityParams.qualityParamLabel = quality.quality.qualityParamLabel
                qualityParams.formulaParam = quality.quality.formulaParam
                qualityParams.qualityParameterValue = quality.quality.qualityParameterValue
                qualityParams.isSyncStatus = quality.quality.isSyncStatus
                qualityParamsQualitative.qualityParameter = qualityParams
                qualityParamsQualitative.qualitative = quality.qualitative
                qualityParamList.add(qualityParamsQualitative)
            }
            qualityList.value = qualityParamList
        }
    }

    return qualityList
}

fun prepareWeighBridgeData(receiving: VegaGrnWeighBridgeId): VegaQualityWBDetails {
    val qualityWB = VegaQualityWBDetails()
    qualityWB.weighBridgeId =
        if (receiving.weighBridgeId?.isNotEmpty()!!) receiving.weighBridgeId.toString() else receiving.wbTempId
    qualityWB.wbTempId = receiving.wbTempId
    qualityWB.item = receiving.item
    qualityWB.direction = receiving.direction.toString()
    qualityWB.weighBridgeType = receiving.weighBridgeType.toString()
    qualityWB.plant = receiving.plantId
    qualityWB.batchNumber = receiving.batchNumber
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (receiving.materialCode?.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = receiving.supplierCode
    qualityWB.supplierName = receiving.supplierName
    qualityWB.bagCount = receiving.bagCount
    qualityWB.grnNumber = receiving.grnNumber.toString()
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.netWeight = receiving.netWeight.toString()
    qualityWB.grossWeight = receiving.grossWeight.toString()
    qualityWB.unitsOfMeasure = receiving.unitsOfMeasure.toString()
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

    return qualityWB
}

fun prepareVegaQualityData(data: VegaQualityParameter): VegaQuality {
    val quality = VegaQuality()
    quality.materialCode = data.materialCode
    quality.descrChar = data.descrChar
    quality.nameChar = data.nameChar
    quality.qualityParameterValue = data.qualityParameterValue
    quality.wbTempId = data.wbTempId!!
    quality.wbid = data.wbid
    quality.currValFm = data.currValFm
    quality.currValTo = data.currValTo
    quality.dataType = data.dataType
    quality.entryObligatory = data.entryObligatory
    quality.numValFm = data.numValFm
    quality.numValTo = data.numValTo
    quality.isSyncStatus = false
    quality.numberDecimals = data.numberDecimals
    quality.numberDigits = data.numberDigits
    quality.unitText = data.unitText
    quality.unitsOfMeasure = data.unitsOfMeasure
    quality.valRelatn = data.valRelatn
    quality.timeStamp = data.timeStamp
    quality.status = data.status
    quality.syncStatusMsg = data.syncStatusMsg
    quality.preSampling = data.preSampling
    quality.vegaMandatory = data.vegaMandatory
    quality.qualityParamLabel = data.qualityParamLabel
    quality.formulaParam = data.formulaParam
    quality.position = data.position

    return quality
}

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun sortByListOfItems(distinctByList: List<VegaQualityParamsWithQualitative>): List<VegaQualityParamsWithQualitative> {
    val sortedList = arrayListOf<VegaQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
}

fun getQualityThresholdLimit(
    beanCount: Double,
    moisture: Double,
    mould: Double,
    slaty: Double,
    admixture: Double
): Boolean {
    var qualityThresholdLimit: Boolean = false
    if (beanCount > 250 && moisture < 10 && mould < 10 && slaty < 15 && admixture < 5) {
        qualityThresholdLimit = true
    }
    return qualityThresholdLimit
}

fun getQualityCode(bc: Double, mould: Double, adMixture: Double): String {
    var qualityCode: String = ""
    var beanCount = bc.roundToInt().toDouble()
    if (beanCount >= 290 && mould < 7 && adMixture < 3.5) {
        qualityCode = "A"
    } else if (beanCount >= 290 && mould < 7 && adMixture >= 3.5) {
        qualityCode = "B"
    } else if (beanCount >= 290 && (mould >= 7 && mould <= 12) && adMixture < 3.5) {
        qualityCode = "C"
    } else if (beanCount >= 290 && (mould >= 7 && mould <= 12) && adMixture >= 3.5) {
        qualityCode = "D"
    } else if (beanCount >= 290 && (mould >= 12) && adMixture < 3.5) {
        qualityCode = "E"
    } else if (beanCount >= 290 && (mould >= 12) && adMixture >= 3.5) {
        qualityCode = "F"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould < 7) && adMixture < 3.5) {
        qualityCode = "G"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould < 7) && adMixture >= 3.5) {
        qualityCode = "H"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould >= 7 && mould <= 12) && adMixture < 3.5) {
        qualityCode = "I"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould >= 7 && mould <= 12) && adMixture >= 3.5) {
        qualityCode = "J"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould >= 12) && adMixture < 3.5) {
        qualityCode = "K"
    } else if ((beanCount >= 270 && beanCount <= 290) && (mould >= 12) && adMixture >= 3.5) {
        qualityCode = "L"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould < 7) && adMixture < 3.5) {
        qualityCode = "M"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould < 7) && adMixture >= 3.5) {
        qualityCode = "N"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould >= 7 && mould <= 12) && adMixture < 3.5) {
        qualityCode = "O"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould >= 7 && mould <= 12) && adMixture >= 3.5) {
        qualityCode = "P"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould >= 12) && adMixture < 3.5) {
        qualityCode = "Q"
    } else if ((beanCount >= 250 && beanCount <= 269) && (mould >= 12) && adMixture >= 3.5) {
        qualityCode = "R"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould < 7) && adMixture < 3.5) {
        qualityCode = "S"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould < 7) && adMixture >= 3.5) {
        qualityCode = "T"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould >= 7 && mould <= 12) && adMixture < 3.5) {
        qualityCode = "U"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould >= 7 && mould <= 12) && adMixture >= 3.5) {
        qualityCode = "V"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould >= 12) && adMixture < 3.5) {
        qualityCode = "W"
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould >= 12) && adMixture >= 3.5) {
        qualityCode = "X"
    } else if ((beanCount < 230) && (mould < 7) && adMixture >= 0) {
        qualityCode = "Y"
    } else if ((beanCount < 230) && (mould >= 7) && adMixture >= 0) {
        qualityCode = "Z"
    }
    return qualityCode
}


fun getNgCashQualityCode(korValue:String, countValue:String): String{
    val kor= korValue.toDouble()?:0.0
    val count= countValue.toDouble()?:0.0

    val rangeKorA = 50.0..55.0
    val rangeCountA = 120.0..180.0

    val rangeKorB = 50.0..55.0
    val rangeCountB = 180.0..189.0

    val rangeKorC = 50.0..55.0
    val rangeCountC = 190.0..199.0

    val rangeKorD = 50.0..55.0
    val rangeCountD = 200.0..300.0

    val rangeKorE = 49.0..49.9
    val rangeCountE = 120.0..180.0

    val rangeKorF = 49.0..49.9
    val rangeCountF = 180.0..189.0

    val rangeKorG = 49.0..49.9
    val rangeCountG = 190.0..199.0

    val rangeKorH = 49.0..49.9
    val rangeCountH = 200.0..300.0

    val rangeKorI = 48.0..48.9
    val rangeCountI = 120.0..180.0

    val rangeKorJ = 48.0..48.9
    val rangeCountJ = 180.0..189.0

    val rangeKorK = 48.0..48.9
    val rangeCountK = 190.0..199.0

    //    L	48.0 - 48.9	200 - 300
    val rangeKorL = 48.0..48.9
    val rangeCountL = 200.0..300.0

    //    M	47.0 - 47.9	120 - 180
    val rangeKorM = 47.0..47.9
    val rangeCountM = 120.0..180.0

    //    N	47.0 - 47.9	180 - 189
    val rangeKorN = 47.0..47.9
    val rangeCountN = 180.0..189.0

    //    O	 47.0 - 47.9	190 - 199
    val rangeKorO = 47.0..47.9
    val rangeCountO = 190.0..199.0

    //    P	47.0 - 47.9	200 - 300
    val rangeKorP = 47.0..47.9
    val rangeCountP = 200.0..300.0

    //    Q	46.0 - 46.9	120 - 180
    val rangeKorQ = 46.0..46.9
    val rangeCountQ = 120.0..180.0

//    R	46.0 - 46.9	180 - 189
    val rangeKorR = 46.0..46.9
    val rangeCountR = 180.0..189.0

    //    S	46.0 - 46.9	190 - 199
    val rangeKorS = 46.0..46.9
    val rangeCountS = 190.0..199.0

    //    T	46.0 - 46.9	200 - 300
    val rangeKorT = 46.0..46.9
    val rangeCountT = 200.0..300.0

    //    U	20 - 46	120 - 180
    val rangeKorU = 20.0..46.0
    val rangeCountU = 120.0..180.0

    //    V	20 - 46	180 - 189
    val rangeKorV = 20.0..46.0
    val rangeCountV = 180.0..189.0

    //    W	20 - 46	190 - 199
    val rangeKorW = 20.0..46.0
    val rangeCountW = 190.0..199.0

    //    X	20 - 46	200 - 300
    val rangeKorX = 20.0..46.9
    val rangeCountX = 200.0..300.0


   val  qualityCode=  when{
       rangeKorA.contains(kor) && rangeCountA.contains(count) -> "A"
       rangeKorB.contains(kor) && rangeCountB.contains(count) -> "B"
       rangeKorC.contains(kor) && rangeCountC.contains(count) -> "C"
       rangeKorD.contains(kor) && rangeCountD.contains(count) -> "D"
       rangeKorE.contains(kor) && rangeCountE.contains(count) -> "E"
       rangeKorF.contains(kor) && rangeCountF.contains(count) -> "F"
       rangeKorG.contains(kor) && rangeCountG.contains(count) -> "G"
       rangeKorH.contains(kor) && rangeCountH.contains(count) -> "H"
       rangeKorI.contains(kor) && rangeCountI.contains(count) -> "I"
       rangeKorJ.contains(kor) && rangeCountJ.contains(count) -> "J"
       rangeKorK.contains(kor) && rangeCountK.contains(count) -> "K"
       rangeKorL.contains(kor) && rangeCountL.contains(count) -> "L"
       rangeKorM.contains(kor) && rangeCountM.contains(count) -> "M"
       rangeKorN.contains(kor) && rangeCountN.contains(count) -> "N"
       rangeKorO.contains(kor) && rangeCountO.contains(count) -> "O"
       rangeKorP.contains(kor) && rangeCountP.contains(count) -> "P"
       rangeKorQ.contains(kor) && rangeCountQ.contains(count) -> "Q"
       rangeKorR.contains(kor) && rangeCountR.contains(count) -> "R"
       rangeKorS.contains(kor) && rangeCountS.contains(count) -> "S"
       rangeKorT.contains(kor) && rangeCountT.contains(count) -> "T"
       rangeKorU.contains(kor) && rangeCountU.contains(count) -> "U"
       rangeKorV.contains(kor) && rangeCountV.contains(count) -> "V"
       rangeKorW.contains(kor) && rangeCountW.contains(count) -> "W"
       rangeKorX.contains(kor) && rangeCountX.contains(count) -> "X"

       else -> ""
   }


   return qualityCode

}
