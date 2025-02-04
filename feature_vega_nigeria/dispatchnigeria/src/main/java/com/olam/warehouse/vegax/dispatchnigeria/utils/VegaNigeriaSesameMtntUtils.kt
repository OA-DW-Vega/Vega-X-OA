package com.olam.warehouse.vegax.dispatchnigeria.utils

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KMutableProperty1


const val MTNT = "mtnt"
const val LOCAL_SALES = "local_sales"
const val EXPORT_DISPATCH = "export"
const val WEIGHBRIDGE = "weighbridge"
const val WEIGHSCALE = "weighscale"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val ANTICIPATED = "anticipated"
const val MODEL_BUNDLE = "model"
const val IS_PARAMS_VALUE = "is_params_values"
const val MTNR = "MTN-R"
const val MTNTNEW = "MTN-T"
const val PROCURE = "PROCURE"
const val WEIGHBRIDGE_ADD_LOT = "weigh_bridge_add_lot"
const val LOT_LIST = "lot_list"
const val WEIGHBRIDGE_SUMMARY = "wb_summary"
const val IS_EDIT = "edit"
const val UPDATE_WEIGHT = "updateWeight"
const val ADD_WEIGHT = "add_weight"
const val WEIGHSCALE_ADD_LOT = "weigh_scale_add_lot"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val WEIGHSCALE_SUMMARY = "ws_summary"
const val DIRECTIONIN = "IN"
const val DIRECTIONOUT = "OUT"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val JSON_PROCESS_TYPE_LIST = "PROCESS_TYPE_LIST"
const val FNQUALITY = ""
const val FNQUALITY_THRESHOLD = "X"
const val FNREJECT = "D"
const val PARAMS_LIST = "params_list"
const val MATERIAL_CODE = "material_no"
const val MERGED_BATCHNUMBER = "merged_BatchNumber"
const val MTNTQUALITY_DATA = "mtntqualityData"
const val WEIGHTED_AVERAGE_LIST = "weightedAverageList"
const val B_MOULD4 = "B_MOULD4"
const val BATCH_NO = "batch_no"
const val FINAL_APPROVAL = "final_approval"
const val STORAGELOCATION_CODE = "storageLocationCode"
const val FLAG = "flag"
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
const val MOISTURE = "moisture"
const val APPROVE_BATCH_DETAILS = "batchDetails"
const val APPROVE_QUALITY_DATA = "approve_quality_data"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getDrawable(id: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getDrawable(id, null)
    } else {
        App.getAppContext().resources.getDrawable(id)
    }
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}

fun convertMtToKg(weight: String?, uom: String): String? {
    if (uom.equals("kg", true)) return weight
    val converted = weight?.toDouble()?.times(1000)
    return converted?.formatThreeDigits()
}

fun convertKgToMT(weight: String?, uom: String): String? {
    if (uom.equals("kg", true)) return weight
    return weight?.toDouble()?.div(1000)?.formatThreeDigits()
}

fun getQualityCode(beanCount: Double, mould: Double, adMixture: Double): String {
    var qualityCode: String = ""
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
    } else if ((beanCount >= 230 && beanCount <= 249) && (mould >= 12) && adMixture < 3.5) {
        qualityCode = "X"
    } else if ((beanCount < 230) && (mould < 7) && adMixture >= 0) {
        qualityCode = "Y"
    } else if ((beanCount < 230) && (mould >= 7) && adMixture >= 0) {
        qualityCode = "Z"
    }
    return qualityCode
}

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

fun sortByListOfItems(distinctByList: List<VegaQualityParamsWithQualitative>): List<VegaQualityParamsWithQualitative> {
    val sortedList = arrayListOf<VegaQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
}

