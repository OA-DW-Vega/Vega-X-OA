package com.olam.warehouse.vegax.inventorycocoa.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */

const val FILTER_MATERIAL = "filter_material"
const val FILTER_LOCATION = "filter_st_location"
const val FILTER_BEAN = "filter_bean"
const val FILTER_MOIST = "filter_moist"
const val FILTER_FFA = "filter_ffa"
const val FILTER_FAT = "filter_fat"
const val FULL_FILTER = "full_filter"
const val SYNC_STATUS_DATA = "sync_status_list"
const val IS_THIRD_PARTY = "third_party"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    }
    else {
        App.getAppContext().resources.getColor(id)
    }
}
