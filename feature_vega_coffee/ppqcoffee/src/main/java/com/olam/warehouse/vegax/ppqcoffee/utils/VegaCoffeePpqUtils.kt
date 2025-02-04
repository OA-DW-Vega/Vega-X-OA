package com.olam.warehouse.vegax.ppqcoffee.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 8/5/2020.
 */
const val NET_WEIGHT = "net_weight"
const val TAR_WEIGHT = "tar_weight"
const val ITEM = "item"
const val BATCH_NO = "batch_no"
const val TRUCK_NO = "truck_no"
const val MATERIAL_NO = "material_no"
const val IS_PARAMS_VALUE = "is_params_values"
const val PARAMS_LIST = "params_list"
const val INSPECTION_LOT = "inspection_lot"
const val PPQ = "ppq"
const val WAREHOUSE = "Warehouse"
const val FNQUALITY = "Q"
const val FNREJECT = "D"
const val DIRECTIONIN = "IN"
const val COPIED_WBID = "copied_wbid"
const val COPIED_MATERIAL = "copied_material"
const val PARAMS_FRAG = "params_frag"
const val ACCEPT = "accept"
const val REJECT = "reject"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
