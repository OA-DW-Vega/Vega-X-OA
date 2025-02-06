package com.olam.warehouse.vegax.ginningwarehouse.ui.utils

import android.os.Build
import androidx.core.content.ContextCompat
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.WAREHOUSE_ID
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

const val TITLE = "title"
const val SUB_TITLE = "sub_title"
const val CONTAINER_ID = "container_id"
const val OT_NUMBER = "ot_number"
const val NEW_CONTAINER = "new_container"
const val ACTIVITY_REQUEST_CODE = 1
const val ACTIVITY_SCAN_REQUEST_CODE = 2
const val SEAL_SUCCESS = "seal_success"
const val DISPATCH_SUCCESS = "dispatch_success"
const val OFFLOAD_SUCCESS = "offload_success"
const val SUCCESS = "success"
const val MTN_ID = "mtn_id"
const val BALE_ID = "bale_id"
const val BALE_COUNT = "bale_count"
const val BALE = "bale"
const val MTN = "mtn"
const val FILTER_GRADE = "filter_grade"
const val FILTER_BALE = "filter_bale"
const val INVENTORY_BALE = "inventory_bale"
const val SELECTED_GRADE = "selected_grade"
const val COTTON_PORT = "cotton-portWH"
const val MATERIAL_DOC_ID = "material_doc_id"
const val SHIFT_VALUE = "SHIFT_CHANGE"
const val LOT_DETAIL = "LOT_DETAIL"
const val AM_7 = 7
const val PM_3 = 15
const val PM_11 = 23
const val BT_MAC = "STORED_BT_MAC"
const val REQUEST_ENABLE_BT = 123
const val DELIVERY_NO = "DELIVERY_NO"
const val BALE_LIST = "BALE_LIST"
const val SELECTED_GRADE_ITEM = 3
const val SELECTED_BALE = "selected_bale"
const val FRGA_DISPATCH_CONFIRM = "frag_dispatch_confirm"
const val FRGA_DISPATCH_ADD_BALE = "frag_dispatch_add_bale"
const val FRGA_DISPATCH_OFFLINE = "frag_dispatch_offline"
const val TRANS_DATA = "trans_data"
const val DISPATCH_DATA = "dispatch_data"
const val DISPATCH_OUTPUT_DATA = "dispatch_output_data"
const val DATA_SYNC = "data_sync"
const val BALE_DATA_SYNC = "bale_data_sync"
const val UNIQUE_ONE_TIME_WORKER_DATA = "unique_data_sync"
const val UNIQUE_ONE_TIME_WORKER_BALE = "unique_bale_data_sync"
const val APPCENTER_SECRET_PROD = "f4d7b0b7-84f6-4370-b7ae-496febb597b9"
const val APPCENTER_SECRET_UAT = "bd6bc4d7-5d98-429c-ac86-958d79bcdd26"

const val PLANT = "plant"
const val GRADES = "grades"
const val INCOMING_MTN = "incoming_mtn"
const val MTN_OUTPUT_DATA = "mtn_work_data"
const val PILE_ID = "pile_id"
const val PILE_LIST = "pile_list"
const val CONFIRM_PILE = "pile_confirm"

fun getWHId() = PreferenceHelper.get(WAREHOUSE_ID, 0)

fun isOnline(): Boolean {
    return PreferenceHelper.get(Constants.USER_ONLINE, true)
}

fun posExtension(tags: MutableSet<String>): Int {
    return if (tags.first().contains("com")) tags.last().toInt() else tags.first().toInt()
}
fun getColorFromId(resourceId: Int) = ContextCompat.getColor(App.getAppContext(), resourceId)

@Suppress("DEPRECATION")
fun getColorUtil(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

