package com.olam.warehouse.vegax.exportsalesnigeria.utils

import android.os.Build
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 9/4/2020.
 */


const val MOVE_ADD_LOT = "move_add_lot"
const val MOVE_SUMMARY = "move_summary"
const val BUNDLE_DATA = "bundle_data"
const val CONTAINER_DATA = "container_data"
const val MATERIAL_LIST = "material_list"
const val TEXTDETAIL_LIST = "textdetail_list"
const val SELECTED_MATERIAL_LIST = "selected_material_list"
const val INVENTORY_FRAG = "inventory_frag"
const val SALES_ID = "sales_order_id"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val ADD_WEIGHT = "add_weight"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val UPDATE_WEIGHT = "updateWeight"
const val MODEL_BUNDLE = "model"
const val TEXT_UPDATE_landingno = "BILL OF LADING NUMBER"
const val TEXT_UPDATE_portloading = "PORT OF LOADING"
const val TEXT_UPDATE_portdischarge = "PORT OF DISCHARGE - DETAILED"
const val TEXT_UPDATE_ladingdate = "BILL OF LADING DATE"
const val TEXT_UPDATE_vesselname = "VESSEL NAME / FLIGHT NO"
const val TEXT_UPDATE_deliveredqty = "DELIVERED QTY"
const val TEXT_UPDATE_shippingline = "SHIPPING LINE"
const val JSON_SHIFT_DETAILS_LIST_EN = "SHIFT_DETAILS_LIST_EN"


@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}
