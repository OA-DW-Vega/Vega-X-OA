package com.olam.warehouse.vegax.gateentry.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
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

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
