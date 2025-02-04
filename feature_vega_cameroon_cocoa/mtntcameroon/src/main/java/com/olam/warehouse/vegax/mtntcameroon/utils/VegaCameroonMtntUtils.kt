package com.olam.warehouse.vegax.mtntcameroon.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1


const val MTNT = "mtnt"
const val LOCAL_SALES = "local_sales"
const val EXPORT_DISPATCH = "export"
const val WEIGHBRIDGE = "weighbridge"
const val WEIGHSCALE = "weighscale"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val ANTICIPATED = "anticipated"
const val MODEL_BUNDLE = "model"
const val WEIGHBRIDGE_ADD_LOT = "weigh_bridge_add_lot"
const val LOT_LIST = "lot_list"
const val WEIGHBRIDGE_SUMMARY = "wb_summary"
const val IS_EDIT = "edit"
const val UPDATE_WEIGHT = "updateWeight"
const val ADD_WEIGHT = "add_weight"
const val WEIGHSCALE_ADD_LOT = "weigh_scale_add_lot"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val WEIGHSCALE_SUMMARY = "ws_summary"
const val DIRECTIONIN = "IN"
const val DIRECTIONOUT = "OUT"


@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getDrawable(id: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getDrawable(id, null)
    } else {
        App.getAppContext().resources.getDrawable(id)
    }
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}
