package com.olam.warehouse.vegax.gateentrycameroon.utils

import android.os.Build
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
