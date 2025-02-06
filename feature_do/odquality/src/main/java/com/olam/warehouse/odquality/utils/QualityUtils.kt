package com.olam.warehouse.odquality.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.master.dorigin.entity.DOQualityParameter
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.master.dorigin.model.DOQualityWithQualitative
import com.olam.warehouse.presentation.enums.CountryCode.VIETNAM
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */

const val NET_WEIGHT = "net_weight"
const val TAR_WEIGHT = "tar_weight"
const val CHALLAN = "challan"
const val AVG_BAG_WEIGHT = "avg_bag_weight"
const val BATCH_NUMBER = "batch_number"
const val PLANT_ID = "plant_id"
const val QC_STATUS = "qc_status"
const val WEIGHBRIDGE_LIST = "weigh_bridge_list"
const val BATCH_NO = "batch_no"
const val MATERIAL_NO = "material_no"
const val IS_PARAMS_VALUE = "is_params_values"
const val QUALITY_OUTPUT_DATA = "quality_work_data"
const val QUALITY_DATA = "quality_data"
const val QUALITY_OFFLINE = "qty_offline"
const val QUALITY_OFFLINE_LIST = "qty_offline_list"
const val PARAMS_LIST = "params_list"
const val UOM = "units_of_measure"
const val GROSS_WEIGHT = "gross_weight"
const val CHOOSE_QR_FOR_SAMPLING = "choose_qr_for_sampling"
const val SELECTED_QUALITY = "selected_quality"
const val SOURCE_LOT_ID = "source_lot_id"

fun prepareData(quality1: List<DOQualityWithQualitative>): LiveData<List<DOQualityParamsWithQualitative>> {
    val qualityParamList = arrayListOf<DOQualityParamsWithQualitative>()
    val qualityList = MutableLiveData<List<DOQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.quality.position }
            qualitySortedBy.forEach { quality ->
                val qualityParamsQualitative = DOQualityParamsWithQualitative()
                val qualityParams = DOQualityParameter()
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
                qualityParams.qualityParameterValue = quality.quality.qualityParameterValue
                qualityParams.isSyncStatus = quality.quality.isSyncStatus
                qualityParams.doMandatory = quality.quality.doMandatory
                qualityParamsQualitative.qualityParameter = qualityParams
                qualityParamsQualitative.qualitative = quality.qualitative
                qualityParamList.add(qualityParamsQualitative)
            }
            qualityList.value = qualityParamList
        }
    }

    return qualityList
}

fun prepareWeighBridgeData(receiving: DOReceiving): DOQualityWBDetails {
    val qualityWB = DOQualityWBDetails()
    qualityWB.weighBridgeId = receiving.tmpWbId
    qualityWB.wbTempId = receiving.tmpWbId
    qualityWB.item = receiving.item
    qualityWB.plant = receiving.plantId
    qualityWB.batchNumber = receiving.charg
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (PreferenceHelper.get(
                Constants.COUNTRY_CODE,
                ""
            ) != VIETNAM.code && receiving.materialCode?.length != 18
        ) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = receiving.supplierCode
    qualityWB.bagCount = receiving.bagCount
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.posnr
    qualityWB.netWeight = receiving.netWeight.toString()
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
    qualityWB.challan = receiving.txnId
    qualityWB.grossWeight = receiving.grossWeight.toString()
    qualityWB.unitsOfMeasure = receiving.uom
    qualityWB.currentKey = receiving.currentKey
    return qualityWB
}

fun prepareDOQualityData(data: DOQualityParameter): DOQuality {
    val quality = DOQuality()
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
    quality.doMandatory = data.doMandatory
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

fun posExtension(tags: MutableSet<String>): Int {
    return if (tags.first().contains("com")) tags.last().toInt() else tags.first().toInt()
}

fun sortByListOfItems(distinctByList: List<DOQualityParamsWithQualitative>): List<DOQualityParamsWithQualitative> {
    val sortedList = arrayListOf<DOQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
}

