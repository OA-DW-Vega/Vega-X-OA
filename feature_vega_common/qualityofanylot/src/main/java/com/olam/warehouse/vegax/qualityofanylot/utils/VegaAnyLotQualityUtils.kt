package com.olam.warehouse.vegax.qualityofanylot.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.presentation.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

const val REJECT = "reject"
const val ACCEPT = "accept"
const val TEMP_ID = "temp_id"
const val WB_ID = "wb_id"
const val DEVICE_ID = "device_id"
const val COPY = "copy"
const val EDIT = "edit"
const val DIRECTIONIN = "IN"
const val DIRECTIONOUT = "OUT"
const val BATCH_NO = "batch_no"
const val FINAL_APPROVAL = "final_approval"
const val CHALLAN = "challan"
const val FLAG = "flag"
const val MOULD_VALUE = "mould_value"
const val ADD_MIXTURE = "add_Mixture"
const val BEAN_WT_GRAM = "bean_weight_gram"
const val BEAN_COUNT = "bean_count"
const val SLATY = "slaty"
const val MOISTURE = "moisture"
const val STORAGELOCATION_CODE = "storageLocationCode"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val APPROVE_BATCH_DETAILS = "batchDetails"
const val FNQUALITY = ""
const val INSPECTION_LOT = "inspection_lot"
const val PARAMS_FRAG = "params_frag"
const val TYPE_SELECT = "type_select"
const val TYPE_POSITION = "type_pos"
const val VIEW_QUALITY_LOT= "view_lot"
const val CREATE_QUALITY_LOT= "create_lot"
const val LOT_ID= "lot_id"
const val VIEW_TRANSACTION_LIST= "transaction_list"
const val ANY_LOT_QUALITY_PARAM= "ANY_LOT_QUALITY_PARAMS"
const val ANY_LOT_QUALITY_REQUEST= "ANY_LOT_QUALITY_REQUEST"
const val B_MOULD4 = "B_MOULD4"
const val NG_ADMIX = "NG_ADMIX"
const val B_DCTBW1 = "B_DCTBW1"
const val ZNGCOCOA_ACTBW = "ZNGCOCOA_ACTBW"
const val B_SL = "B_SL"
const val B_MOIST = "B_MOIST"



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

fun prepareQualitative(
    custonLocationList: MutableList<VegaCustomStLocation>,
    materialCode: String
): List<VegaQualitative> {
    val qualityParamList = arrayListOf<VegaQualitative>()
    custonLocationList.forEach {
        val quality = VegaQualitative()
        quality.charValue = it.procureLocationCode.toString()
        quality.nameChar = it.procureLocationCode.toString()
        quality.materialCode = materialCode.toString()
        qualityParamList.add(quality)
    }
    return qualityParamList
}

fun sortByListOfItems(distinctByList: List<VegaQualityParamsWithQualitative>): List<VegaQualityParamsWithQualitative> {
    val sortedList = arrayListOf<VegaQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
}

fun getTmpId() = "TMP_".plus(Random.nextLong().toString().take(5)).plus(DateUtils.getCurrentTimeInMills().toString().takeLast(5))
