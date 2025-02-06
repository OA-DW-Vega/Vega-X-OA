package com.olam.warehouse.vegax.exportsalesecuador.utils

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
const val SELECTED_MATERIAL_LIST = "selected_material_list"
const val INVENTORY_FRAG = "inventory_frag"
const val SALES_ID = "sales_order_id"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"


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
