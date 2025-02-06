package com.olam.warehouse.vegax.inventoryghanacocoa.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Keerthi Santhanam on 7/06/2020.
 */
const val FILTER_MATERIAL = "filter_material"
const val FILTER_IMPURITY = "filter_impurity"
const val FILTER_HUMIDITY = "filter_humidity"
const val FILTER_MOULD = "filter_mould"
const val FULL_FILTER = "full_filter"
const val SYNC_STATUS_DATA = "sync_status_list"
const val INVENTORY_DETAILS = "inventory_details"
const val TRANSIT_INVENTORY_DETAILS = "transit_inventory_details"
const val INVENTORY_LOTS = "lots"
const val INVENTORY_CODE = "code"
const val FILTER_LIST = "filter_list"
const val STORAGE = "storage"
const val TRANSIT = "transit"
const val BAG = "BAG"
const val MATERIAL_CODE = "000000"
const val BAGS = "BAGS"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
