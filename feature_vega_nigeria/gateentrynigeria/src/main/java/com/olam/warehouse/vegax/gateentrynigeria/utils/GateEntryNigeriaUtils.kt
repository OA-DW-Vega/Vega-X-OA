package com.olam.warehouse.vegax.gateentrynigeria.utils

import android.os.Build
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
const val SUPPLIER = "Supplier"
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
const val PR = "PR"
const val PX = "PX"
const val Z01 = "Z01"
const val DIS = "DIS"
const val GRN_VALUE = "101"
const val GATE_ENTRY_PENDING = "pending"
const val JSON_PROCUREMENT_TYPE = "PROCUREMENT_TYPE"
const val SUPPLIER_CODE = "000"
const val DD = "DD"
const val DX = "DX"
const val WEIGHBRIDGE = "weighbridge"
const val WEIGHSCALE = "weighscale"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val WS = "WS"
const val WB = "WB"
const val PRODUCT = "Product"
const val PLANT = "plant"
const val RECEIVING_LOCATION = "receiving_location"

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

fun isNGCashewEnabled(): Boolean {
    return getCurrentKey().split("_")[1].contains("NG")&& getCurrentKey().split("_")[2].contains("CASH")
}
fun createRandomInteger(aStart: Int, aEnd: Long, aRandom: Random): String {
//    require(aStart <= aEnd) { "Start cannot exceed End." }
//    //get the range, casting to long to avoid overflow problems
//    val range = aEnd - aStart.toLong() + 1
//    // compute a fraction of the range, 0 <= frac < range
//    val fraction = (range * aRandom.nextDouble()).toLong()
//    val randomNumber = (fraction + aStart.toLong()).toString()
    val timeMilLis = DateUtils.getCurrentTimeInMills().toString()
    return timeMilLis.takeLast(10)
}



