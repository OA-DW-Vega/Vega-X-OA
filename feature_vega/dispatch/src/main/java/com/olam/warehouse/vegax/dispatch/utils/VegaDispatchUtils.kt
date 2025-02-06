package com.olam.warehouse.vegax.dispatch.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Keerthi Santhanam on 2/11/2020.
 */
const val DISPATCH_DATA = "dispatch_intent_data"
const val DISPATCH_DATA_LIST = "dispatch_intent_data_list"
const val DIRECTION = "OUT"
const val DISPATCH_PROCESS = "dispatch_process"
const val DISPATCH_SUMMARY_FRAG = "dispatch_summary_frag"
const val BATCH_NUMBER = "batch_number"
const val MATERIAL = "material"
const val REGION = "region"
const val KOR = "kor"
const val QUALITY_PARAMS_DATA = "quality_params_data"

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
