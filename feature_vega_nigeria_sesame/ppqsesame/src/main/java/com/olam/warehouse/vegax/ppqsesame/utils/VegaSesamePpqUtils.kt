package com.olam.warehouse.vegax.ppqsesame.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 8/5/2020.
 */
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
const val FNQUALITY = "Q"
const val FNREJECT = "D"
const val DIRECTIONIN = "IN"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
const val PARAMS_FRAG = "params_frag"
const val ACCEPT = "accept"
const val REJECT = "reject"
var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()

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

fun prepareQualitativeNic(
    storageLocationList: MutableList<VegaStorageLocation>,
    materialCode: String
): List<VegaQualitative> {
    val qualityParamList = arrayListOf<VegaQualitative>()
    storageLocationList.forEach {
        val quality = VegaQualitative()
        quality.charValue = it.storageLocationCode
        quality.nameChar = it.storageLocationCode
        quality.materialCode = materialCode
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
