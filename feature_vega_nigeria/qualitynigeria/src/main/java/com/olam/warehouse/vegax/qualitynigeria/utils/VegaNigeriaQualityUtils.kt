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

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
const val APPROVE_BATCH_DETAILS = "batchDetails"
const val APPROVE_QUALITY_DATA = "approve_quality_data"
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
