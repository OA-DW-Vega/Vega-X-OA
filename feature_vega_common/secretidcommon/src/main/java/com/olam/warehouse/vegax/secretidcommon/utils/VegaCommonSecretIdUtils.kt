package com.olam.warehouse.vegax.secretidcommon.utils

import kotlin.reflect.KMutableProperty1

const val MODULE_SELECT = "module_select"
const val LOT_LIST = "lot_list"
const val OFFLOADING_SELECT = "offloading_select"


inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}
