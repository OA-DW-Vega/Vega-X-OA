package com.olam.warehouse.vegax.mtntindo.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.vegax.App
import kotlin.random.Random
import kotlin.reflect.KMutableProperty1

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */

const val MTNT = "mtnt"
const val ADD_LOT = "add_lot"
const val ADD_LOT_TRANS = "add_lot_trans"
const val LOT_LIST = "lot_list"
const val MATERIAL_LIST = "material_list"
const val MODEL_BUNDLE = "model_bundle"
const val MULTIPLE_LOT = "multiple_lot"
const val MERGE_LIST = "merge_list"
const val MERGE_PREVIEW = "merge_preview"
const val DISPATCH_SUMMARY = "dispatch_summary"
const val DISPATCH_OFFLINE_SUMMARY = "dispatch_offline_summary"
const val DISPATCH_DATA_LIST = "dispatch_intent_data_list"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

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

fun prepareLotsList(it1: MutableList<VegaEcuadorDispatchStocks>): MutableList<VegaEcuadorDispatchLots> {
    val lotList = mutableListOf<VegaEcuadorDispatchLots>()
    it1.forEach { item ->
        val lot = VegaEcuadorDispatchLots()
        lot.batchNumber = item.batchNumber
        lot.materialCode = item.materialCode
        lot.materialName = item.materialName
        lot.plantId = item.plantId
        lot.plantName = item.plantName
        lot.storageLocationCode = item.storageLocationCode
        lot.unitsOfMeasure = item.unitOfMeasure
        lot.netWeight = item.weight
        lot.grossWeight = item.weight
        lotList.add(lot)
    }
    return lotList
}
