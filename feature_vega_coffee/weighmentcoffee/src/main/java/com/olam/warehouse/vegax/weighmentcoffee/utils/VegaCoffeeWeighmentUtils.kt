package com.olam.warehouse.vegax.weighmentcoffee.utils

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 9/3/2020.
 */

const val SUPPLIER = "supplier"
const val MTNR = "mtnr"
const val EXPORT_SALES = "export_sales"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val ADD_WEIGHT = "add_weight"
const val UPDATE_WEIGHT = "update_weight"
const val FRAG_ADD_BAG_WEIGHT = "add_bag_weight"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val THIRD_PARTY_SALES = "thirdParty"
const val LOCAL_SALES = "localSales"

const val WB01 = "WB01"
const val WAREHOUSE = "Warehouse"
const val MTNT = "mtnt"
const val PROCURE = "PROCURE"
const val SALES = "SALES"
const val STO = "STO"
const val DIRECTIONIN = "IN"
const val DIRECTIONOUT = "OUT"
const val RECEIVING_DATA = "receiving_intent_data"
const val RECEIVING_POST_DATA = "receiving_intent_post_data"
const val RECEIVING_OUTPUT_DATA = "receiving_work_data"
const val MTNT_DATA = "mtnt_intent_data"
const val MTNTDATA = "mtnt_data"
const val MTNT_POST_DATA = "mtnt_intent_data"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val MTNR_FRAG = "mtnr_frag"
const val SUPPLIER_FRAG = "supplier_frag"
const val TRUCKOUT_LIST_FRAG = "truckout_list_frag"
const val TRUCKOUT_ADD_WEIGHT_FRAG = "truckout_add_weight_frag"
const val TRUCKOUT_MTNT_ADD_WEIGHT_FRAG = "truckout_mtnt_add_weight_frag"
const val TRUCKOUT_SUMMARYT_FRAG = "truckout_summary_frag"
const val TRUCKIN_SUMMARYT_FRAG = "truckin_summary_frag"
const val TRUCKIN_MTNT_FRAG = "truckin_mtnt_frag"
const val MATERIAL_CODE = "000000"
const val TRUCKIN_SALES_SUMMARYT_FRAG = "truckin_sales_summary_frag"
const val TRUCK_OUT_SALES_ADD_WEIGHT = "sale_truck_out_add_weight"
const val TRUCKOUT_SALES_SUMMARYT_FRAG = "truckout_sales_summary_frag"
const val PTBF = "Z001"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())
const val MATERIAL_APPEND = "000000"

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

fun getLineItemFromMtnt(postData: MutableList<VegaMtnt>): List<VegaMtntLineItem> {
    val items = mutableListOf<VegaMtntLineItem>()
    postData.forEach {
        val item = VegaMtntLineItem()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.batchNumber = it.batchNumber
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        item.doWeightThreshold = it.doWeightThreshold
        item.truckDirection = it.truckDirection
        item.imagePath = it.imagePath
        item.imageString = it.imageString
        item.vehicleNumber = it.vehicleNumber
        item.vehicleType = it.vehicleType
        item.contactNumber = it.contactNumber
        item.driverName = it.driverName
        item.erdat = it.erdat
        item.ertim = it.ertim
        item.direction = it.direction
        item.transportVendorCode = it.transportVendorCode
        item.transportVendorName = it.transportVendorName
        item.storageLocationCode = it.storageLocationCode
        item.recStorageLocationCode = it.recStorageLocationCode
        item.recPlantId = it.recPlantId
        item.bagTareWeight = it.bagTareWeight
        items.add(item)
    }
    return items
}

fun getLineItemFromReceiving(postData: MutableList<VegaReceiving>): List<VegaReceivingLineItem> {
    val items = mutableListOf<VegaReceivingLineItem>()
    postData.forEach {
        val item = VegaReceivingLineItem()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.bagTareWeight = it.bagTareWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.charg = it.charg
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        items.add(item)
    }
    return items
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun getLineItemFromReceivingLineItem(postData: MutableList<VegaReceivingLineItem>): ArrayList<VegaReceiving> {
    val items = ArrayList<VegaReceiving>()
    postData.forEach {
        val item = VegaReceiving()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.bagTareWeight = it.bagTareWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.charg = it.charg
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight.toString()
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        items.add(item)
    }
    return items
}

fun getMtntFromMtntLineItem(postData: MutableList<VegaMtntLineItem>): ArrayList<VegaMtnt> {
    val items = ArrayList<VegaMtnt>()
    postData.forEach {
        val item = VegaMtnt()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.batchNumber = it.batchNumber
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight.toString()
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        item.doWeightThreshold = it.doWeightThreshold
        item.truckDirection = it.truckDirection
        item.imagePath = it.imagePath
        item.imageString = it.imageString
        item.vehicleNumber = it.vehicleNumber
        item.vehicleType = it.vehicleType
        item.contactNumber = it.contactNumber
        item.driverName = it.driverName
        item.erdat = it.erdat
        item.ertim = it.ertim
        item.direction = it.direction
        item.transportVendorCode = it.transportVendorCode
        item.transportVendorName = it.transportVendorName
        item.storageLocationCode = it.storageLocationCode
        item.recStorageLocationCode = it.recStorageLocationCode
        item.recPlantId = it.recPlantId
        item.bagTareWeight = it.bagTareWeight
        items.add(item)
    }
    return items
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
