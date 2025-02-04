package com.olam.warehouse.vegax.mtntcocoa.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

const val WEIGHBRIDGE = "mtnt_weighbridge"
const val NO_WEIGHMENT = "no_weighment"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val NO_WEIGHMENT_ADD_LOT = "no_weighment_add_lot"
const val SELECT_DISPATCH_TYPE = "typesSelect"
const val NO_WEIGHMENT_SUMMARY = "noWeighmentSummary"
const val LOT_LIST = "no_weighment_lot_list"
const val MTNT_LOT_LIST = "mtnt_lot_list"
const val MODEL_BUNDLE = "model"
const val UPDATE_WEIGHT = "updateWeight"
const val ADD_WEIGHT = "add_weight"
const val WEIGHSCALE_ADD_LOT = "weigh_scale_add_lot"
const val MTNT_WEIGHSCALE_ADD_LOT = "mtnt_weigh_scale_add_lot"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val TRANSACTION_SUMMARY = "transactionSummary"
const val NO_WEIGHMENT_FROM_PENDING = "no_weighment_pending"
const val WEIGHSCALE_SUMMARY = "ws_summary"
const val WEIGHSCALE = "WeighScale"
const val WEIGHSCALE_LOT_LIST = "weighscale_lot_list"
const val WEIGHSCALE_ADD_WEIGHT = "weighscale_add_weight"
const val UPDATE_WEIGHT_WEIGHSCALE = "updateWeight_Weighscale"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val WEIGH_METHOD_WB = "WB"



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

fun prepareOfflineStockInfo(lot: List<VegaEcuadorDispatchStocks>): ArrayList<VegaCocoaNoWeighmentLot> {
    val lotList = ArrayList<VegaCocoaNoWeighmentLot>()
    lot.forEach {
        val batch = VegaCocoaNoWeighmentLot()
        batch.batchNumber = it.batchNumber
        batch.materialCode = it.materialCode
        batch.materialName = it.materialName
        batch.plantId = it.plantId
        batch.status = 1
        batch.plantName = it.plantName
        batch.storageLocationCode = it.storageLocationCode
        batch.unitOfMeasure = it.unitOfMeasure
        batch.weight = it.weight
        batch.vendor = it.vendor
        batch.vendorName = it.vendorName
        lotList.add(batch)
    }
    return lotList
}

