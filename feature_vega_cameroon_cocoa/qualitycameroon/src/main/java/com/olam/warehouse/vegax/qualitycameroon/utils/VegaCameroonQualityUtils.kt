package com.olam.warehouse.vegax.qualitycameroon.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualitySecretId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */

const val WEIGHMENT_TYPE = "weighment_type"
const val WEIGHBRIDGE_LIST = "weigh_bridge_list"
const val WEIGHBRIDGE_LIST_MTNR = "weigh_bridge_list_mtnr"
const val QUALITY_OFFLINE = "qty_offline"
const val QUALITY_OFFLINE_LIST = "qty_offline_list"
const val MTNR = "MTN-R"
const val SUPPLIER = "Supplier"
const val PARAMS_LIST = "params_list"
const val PARAMS_LIST_MTNR = "params_list_mtnr"
const val LOT_LIST = "lot_list"
const val LOT = "lot"
const val QUALITY_LIST = "quality_list"
const val WB_ID = "wb_id"
const val FINAL_APPROVAL = "final_approval"
const val QUALITY_SUMMARY = "quality_summary"
const val WEIGHBRIDGE = "weigh_bridge"
const val WEIGHBRIDGE_LIST_TYPE = "weigh_bridge_list_type"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
const val IS_PARAMS_VALUE = "is_params_values"
const val BATCH_NO = "batch_no"
const val MATERIAL_NO = "material_no"
const val WEIGHSCALE = "weigh_scale"
const val FNQUALITY = "Q"
const val FNREJECT = "D"
const val WAREHOUSE = "Warehouse"
const val NET_WEIGHT = "net_weight"
const val TAR_WEIGHT = "tar_weight"
const val CHALLAN = "challan"
const val FLAG = "flag"
const val AVG_BAG_WEIGHT = "avg_bag_weight"
const val BATCH_NUMBER = "batch_number"
const val GROSS_WEIGHT = "gross_weight"
const val JSON_PROCESS_TYPE_LIST = "PROCESS_TYPE_LIST"


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

fun prepareWeighTypeData(data: List<VegaCameroonQualitySecretId>?): List<VegaQualityWBDetails> {
    var list = ArrayList<VegaQualityWBDetails>()
    data?.forEach { receiving ->
        val qualityWB = VegaQualityWBDetails()
        qualityWB.weighBridgeId =
            if (receiving.weighBridgeId.isNotEmpty()) receiving.weighBridgeId.toString() else receiving.weighBridgeId
        qualityWB.wbTempId = receiving.weighBridgeId
        qualityWB.item = receiving.item
        qualityWB.direction = receiving.direction.toString()
        qualityWB.qcStatus = receiving.qcFlag
        qualityWB.weighBridgeType = receiving.wtype.toString()
        qualityWB.plant = receiving.plant
        qualityWB.batchNumber = receiving.batchNumber
        qualityWB.materialName = receiving.materialName
        qualityWB.materialCode =
            if (receiving.materialCode.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
        qualityWB.supplierCode = receiving.supplierCode
//    qualityWB.supplierName = receiving.supplierName
        qualityWB.bagCount = receiving.bagCount
        qualityWB.grnNumber = receiving.grnNumber.toString()
        qualityWB.bagType = receiving.pmat1
        qualityWB.bagWeight = receiving.wsPkwgt.toString()
//    qualityWB.deliveryItem = receiving.deliveryItem
        qualityWB.netWeight = receiving.netWeight.toString()
        qualityWB.grossWeight = receiving.grossWeight.toString()
        qualityWB.unitsOfMeasure = receiving.unitOfMeasure.toString()
        qualityWB.isNotWBID = true
        qualityWB.purchaseDocDesc = receiving.ebelp
        qualityWB.purchaseDocNum = receiving.ebeln
        qualityWB.delivery = receiving.posnr
        qualityWB.challan = receiving.challan
        qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

        list.add(qualityWB)
    }
    return list
}
