package com.olam.warehouse.vegax.weighment.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.vegax.App
import kotlin.random.Random


const val WB01 = "0002"
const val SUPPLIER = "Supplier"
const val WAREHOUSE = "Warehouse"
const val MTNR = "mtnr"
const val MTNT = "mtnt"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val DIRECTIONIN = "IN"
const val DIRECTIONOUT = "OUT"
const val RECEIVING_DATA = "receiving_intent_data"
const val RECEIVING_POST_DATA = "receiving_intent_post_data"
const val RECEIVING_OUTPUT_DATA = "receiving_work_data"
const val MTNT_DATA = "mtnt_intent_data"
const val MTNTDATA = "mtnt_data"
const val MTNT_POST_DATA = "mtnt_intent_data"

const val MTNR_FRAG = "mtnr_frag"
const val SUPPLIER_FRAG = "supplier_frag"
const val TRUCKOUT_LIST_FRAG = "truckout_list_frag"
const val TRUCKOUT_ADD_WEIGHT_FRAG = "truckout_add_weight_frag"
const val TRUCKOUT_MTNT_ADD_WEIGHT_FRAG = "truckout_mtnt_add_weight_frag"
const val TRUCKOUT_SUMMARYT_FRAG = "truckout_summary_frag"
const val TRUCKIN_SUMMARYT_FRAG = "truckin_summary_frag"
const val TRUCKIN_MTNT_FRAG = "truckin_mtnt_frag"
const val MATERIAL_CODE = "000000"
const val ISROUNDOFF = "isRoundoff"
const val VENDOR = "isVendor"
const val PRODUCT = "product"


fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

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

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}


