package com.olam.warehouse.vegax.gateentryapprovalnigeria.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrynigeria.utils.isNGCashewEnabled
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
const val SUPPLIER = "Supplier"
const val PRODUCT = "Product"
const val MTNR = "MTN-R"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val GATE_ENTRY_DATA = "gate_entry_intent_data"
const val GATE_ENTRY_PLANT_DETAILS = "gate_entry_intent_plant_details"
const val DIRECTIONIN = "IN"
const val WAREHOUSE = "Warehouse"
const val PARAMS_LIST_FRAG = "params_list_frag"
const val SUMMARY_FRAG = "summary_frag"
const val MATERIAL_CODE = "000000"
const val WB01 = "WB01"
const val WS01 = "WS01"
const val WS = "WS"
const val WB = "WB"
const val MATERIALNAME = "material_name"
const val UOM = "uom"
const val VENDORNAME = "vendor_name"
const val GATE_ENTRY_COMPLETED = "A"
const val GATE_ENTRY_REJECTED = "R"
const val GATE_ENTRY_PENDING = "pending"
const val WEIGHBRIDGE = "weighbridge"
const val WEIGHSCALE = "weighscale"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val PLANT = "plant"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getRandomNumber(start: Int, end: Int): Int {
    require(start <= end) { "Illegal Argument" }
    return (start..end).random()
}

fun createRandomInteger(aStart: Int, aEnd: Long, aRandom: Random): Long {
    require(aStart <= aEnd) { "Start cannot exceed End." }
    //get the range, casting to long to avoid overflow problems
    val range = aEnd - aStart.toLong() + 1
    // compute a fraction of the range, 0 <= frac < range
    val fraction = (range * aRandom.nextDouble()).toLong()
    val randomNumber = fraction + aStart.toLong()
    return randomNumber
}

fun prepareGateEntryApprovalPost(
    gateEntryData: MutableList<VegaGateEntryDetails>,
    materialName: String,
    vendorName: String,
    year: String,
    uom: String
): List<VegaGateEntry> {
    var postData = mutableListOf<VegaGateEntry>()
    gateEntryData.forEach { it1 ->
        var item = VegaGateEntry()
        item.contactNumber = it1.driverNumber
        item.driverName = it1.driverName
        item.driverNumber = it1.driverNumber
        item.grnModel = it1.grnModel
        item.challan = it1.challan
        item.procurementType = it1.procurementType
        item.item = it1.item
        item.materialCode = it1.materialCode
        item.materialName = materialName
        item.plantId = it1.plant.plantId
        item.storageLocationCode = it1.storageLocationCode
        item.supplierCode = it1.vendorCode
        item.supplierName = vendorName
        item.truckDriverName = it1.driverNumber
        item.unitsOfMeasure = uom
        item.year = year
        item.vehicleNumber = it1.truckNumber
        item.weighBridgeType = it1.wtype.toString()
        item.wsGate = it1.wsgate.toString()
        if (it1.wsgate.equals("WS01")) {
            item.wsType = WS
            item.wsGate = WS01
        } else {
            item.wsType = WB
            item.wsGate = WB01
        }
        item.tmpWbId = it1.wbid.toString()
        item.challan = it1.sampleId
        item.grntNumber = it1.grntNumber
        //if(it1.imageString.isNullOrEmpty()){
        item.imageString = null
        if(isNGCashewEnabled()){
            item.remarks = it1.remarks
            item.weighBridgeId= it1.wbid.toString()
        }
        /*}else {
            item.imageString = it1.imageString
        }*/
        postData.add(item)
    }
    return postData
}
