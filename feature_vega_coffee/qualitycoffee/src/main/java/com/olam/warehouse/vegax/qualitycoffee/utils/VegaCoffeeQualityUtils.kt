package com.olam.warehouse.vegax.qualitycoffee.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.*
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
const val CHALLAN = "challan"
const val ITEM = "item"
const val WEIGHMENT_TYPE = "weigh_ment_type"
const val WEIGHBRIDGE_LIST = "weigh_bridge_list"
const val WEIGHBRIDGE = "weigh_bridge"
var BATCH_NO = "batch_no"
const val TRUCK_NO = "truck_no"
const val MATERIAL_NO = "material_no"
const val IS_PARAMS_VALUE = "is_params_values"
const val QUALITY_OUTPUT_DATA = "quality_work_data"
const val QUALITY_DATA = "quality_data"
const val QUALITY_OFFLINE = "qty_offline"
const val QUALITY_OFFLINE_LIST = "qty_offline_list"
const val PARAMS_LIST = "params_list"
const val LOT_LIST = "lot_list"
const val LOT = "lot"
const val WEIGHBRIDGE_LIST_TYPE = "weigh_bridge_list_type"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val SUPPLIER = "Supplier"
const val PPQ = "ppq"
const val WAREHOUSE = "Warehouse"
const val MTNR = "MTN-R"
const val FNQUALITY = "Q"
const val TYPE_T = "Ticket"
const val TYPE_SAMPLE = "Other"
const val TYPE_R = "Receipt"
const val QUALITY = "QUALITY"
const val QUALITYGRADEDESC = "QUALITYGRADEDESC"
const val FNREJECT = "D"
const val DIRECTIONIN = "IN"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
var grade = "grade"
var certificate = "certificate"
var BAG_COUNT = "BAG COUNT"
var GROSS_WEIGHT = "GROSS WEIGHT"
var IsGain = 0
var IsGainWeight = 0.0
var receivingData = VegaReceiving()
var tallysheet = ""
var weighBridgeId = ""
var deliveryID = ""
const val FGRN_QUALITY = "fgrn_quality"
var materialQualityGradeList = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
var countOfLot = 0
var flag = false
const val MTNR_PRINT_KEY = "mtnr_print_key"
const val MTNR_PRINT_SAMPLE_KEY = "mtnr_print_sample_key"
const val PRINT_TYPE = "print_type"

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

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
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

fun prepareVegaQualityListData(qualityData: List<VegaQualityParameter?>): List<VegaQuality> {
    val qualityList = arrayListOf<VegaQuality>()
    qualityData.forEach { data ->
        data?.let {
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
            qualityList.add(quality)
        }
    }

    return qualityList
}

