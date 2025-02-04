package com.olam.warehouse.vegax.lotqualitynigeria.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val NET_WEIGHT = "net_weight"
const val TAR_WEIGHT = "tar_weight"
const val ITEM = "item"
const val BATCH_NO = "batch_no"
const val TRUCK_NO = "truck_no"
const val MATERIAL_NO = "material_no"
const val IS_PARAMS_VALUE = "is_params_values"
const val PARAMS_LIST = "params_list"
const val INSPECTION_LOT = "inspection_lot"
const val PPQ = "ppq"
const val WAREHOUSE = "Warehouse"
const val FNQUALITY_THRESHOLD = "X"
const val FNQUALITY = ""
const val FNREJECT = "D"
const val DIRECTIONIN = "IN"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
const val PARAMS_FRAG = "params_frag"
const val ACCEPT = "accept"
const val REJECT = "reject"
const val B_MOULD4 = "B_MOULD4"
const val NG_ADMIX = "NG_ADMIX"
const val B_DCTBW1 = "B_DCTBW1"
const val B_BEANCOUNT = "B_BEANCOUNT"
const val B_SL = "B_SL"
const val CHALLAN = "challan"
const val B_MOIST = "B_MOIST"
const val MOULD_VALUE = "mould_value"
const val ADD_MIXTURE = "add_Mixture"
const val BEAN_WT_GRAM = "bean_weight_gram"
const val BEAN_COUNT = "bean_count"
const val SLATY = "slaty"
const val MOISTURE = "moisture"
const val APPROVE_BATCH_DETAILS = "batchDetails"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
const val FINAL_APPROVAL = "final_approval"
const val STORAGELOCATION_CODE = "storageLocationCode"
const val FLAG = "flag"
const val SUMMARY_LIST = "summary_list"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
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
