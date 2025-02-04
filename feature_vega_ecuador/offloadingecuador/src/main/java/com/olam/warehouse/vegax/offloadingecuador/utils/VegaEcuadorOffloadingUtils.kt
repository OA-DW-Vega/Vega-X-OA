package com.olam.warehouse.vegax.offloadingecuador.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */

const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val MATERIAL_CODE = "000000"
const val UNITS_OF_MEASURE = "units_of_measure"
const val BAG_MATERIAL = "bag_material"
const val MATERIAL_NAME = "material_name"
const val WS01 = "WS01"
const val PROCURE = "PROCURE"
const val OFFLOADING_SUMMARY_FRAG = "offloading_summary_frag"
const val OFFLOADING_ADD_WEIGHT_FRAG = "offloading_add_weight_frag"
const val OFFLOADING_DATA = "offloading_intent_data"
const val OFFLOADING_POST_DATA = "offloading_intent_post_data"
const val OFFLOADING_POST_BAG_DATA = "offloading_intent_post_bag_data"
const val OFFLOADING_OFFLINE = "offloading_offline"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
