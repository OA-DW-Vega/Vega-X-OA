package com.olam.warehouse.vegax.inventorynigeria.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Roshna Parambil on 9/3/2020.
 */

const val FILTER_LOCATION = "filter_st_location"
const val FILTER_BEAN = "filter_bean"
const val FILTER_MOIST = "filter_moist"
const val FILTER_FFA = "filter_ffa"
const val FILTER_FAT = "filter_fat"
const val SYNC_STATUS_DATA = "sync_status_list"
const val FILTER_LIST = "filter_list"
const val KEY_CASHEW = "CASH"
const val KEY_SESAME = "SESA"
const val KEY_NIGERIA_COCOA = "COCO"
const val INVENTORY_LOTS = "lots"
const val INVENTORY_CODE = "code"
const val INVENTORY_DETAILS = "inventory_details"
const val FILTER_MATERIAL = "filter_material"
const val FILTER_IMPURITY = "filter_impurity"
const val FILTER_HUMIDITY = "filter_humidity"
const val FILTER_MOULD = "filter_mould"
const val FULL_FILTER = "full_filter"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
