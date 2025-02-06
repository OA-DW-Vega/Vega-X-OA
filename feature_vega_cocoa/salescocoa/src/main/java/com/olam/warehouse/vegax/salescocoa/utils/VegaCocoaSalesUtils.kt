package com.olam.warehouse.vegax.salescocoa.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

const val SALES_TYPE_WEIGHBRIDGE = "Weighbridge"
const val SALES_TYPE_WEIGHSCALE = "Weighscale"
const val SALES_TYPE_ANTICIPATED = "Anticipated"
const val SALES_TYPE_ANTICIPATED_VIRTUAL = "AnticipatedVirtual"
const val BRIDE = "Bridge"
const val SCALE = "Scale"
const val UPDATE_WEIGHT = "UpdateWeight"
const val ADD_LOT = "Add_Lot"
const val SUMMARY = "Summary"
const val PENDING = "Pending"
const val BUNDLE_MODEL = "data"
const val BUNDLE_TYPE = "type"
const val DATE_FORMAT = "dd/MM/yyyy"
const val ADD_WEIGHT = "AddWeight"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val SEARCH_HINT = "Search truck Item"
const val MATERIAL_LIST = "material_list"
const val MODEL_BUNDLE = "model_bundle"
const val ROUND_OFF = "Round_Off"

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

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}
