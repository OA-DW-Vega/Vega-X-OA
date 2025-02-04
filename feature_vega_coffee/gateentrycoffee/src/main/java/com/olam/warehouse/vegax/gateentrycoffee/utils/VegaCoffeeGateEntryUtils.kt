package com.olam.warehouse.vegax.gateentrycoffee.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import kotlin.random.Random

const val SUPPLIER = "Supplier"
const val MTNR = "MTN-R"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val GATE_ENTRY_DATA = "gate_entry_intent_data"
const val DIRECTIONIN = "IN"
const val WAREHOUSE = "Warehouse"
const val PARAMS_LIST_FRAG = "params_list_frag"
const val SUMMARY_FRAG = "summary_frag"
const val MATERIAL_CODE = "000000"
const val WB01 = "WB01"
const val WS01 = "WS01"
const val WEIGHSCALE = "weighScale"
const val WEIGHBRIDGE_WEIGHSCALE = "weighbridge"
const val WEIGHMENT_TYPE = "select_weighment"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())
