package com.olam.warehouse.vegax.stockrecon.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

/**
 * Created by Baskaran Kannan on 6/26/2023.
 */

const val STOCK_RECON_AUDIT_TYPE_SELECT = "stock_recon_audit_type_select"
const val STOCK_RECON_ADD_LOT = "stock_recon_add_lot"
const val STOCK_RECON_INPROGRESS_COMPLETED = "stock_recon_inprogress_completed"
const val STOCK_RECON_LOT_LIST = "stock_recon_lot_list"
const val STOCK_RECON_BAG_AUDIT = "stock_recon_bag_audit"
const val STOCK_RECON_SUMMARY = "stock_recon_summary"
const val STOCK_ALL_AUDIT_LIST = "stock_all_audit_list"
const val RECON_REPORT_DATE_PLANT_SELECTION = "recon_report_date_plant_selection"
const val RECON_REPORT_STATISTICS = "recon_report_statistics"
const val RECON_REPORT_RECON_LIST = "recon_report_recon_list"
const val RECON_REPORT_AUDIT_LIST = "recon_report_audit_list"
const val RECON_REPORT_AUDIT_DETAILS = "recon_report_audit_details"
const val RECON_REPORT_VIEW_PHOTO = "recon_report_view_photo"
const val AUDIT_TYPE = "audit_type"
const val PLANT_DETAILS = "plant_details"
const val STORAGE_LOCATION_DETAILS = "storage_location_details"
const val RECON_ID_DETAILS = "recon_id_details"
const val AUDIT_DETAILS = "audit_details"
const val SELECTED_LOTS = "selected_lotsF"
const val BUNDLE_DATA = "bundle_data"
const val MATERIAL_LIST = "material_list"
const val MODEL_BUNDLE = "model_bundle"
const val DISPATCH_LOTS = "dispatch_lots"
const val DAMAGED_BAGS = "damaged_bags"
const val SPILLAGE = "spillage"
const val BAG_COUNT = "bag_count"
const val STATUS_COMPLETE = "completed"
const val STATUS_CANCEL = "canceled"
const val SINGLE_SPACE = " "

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}

fun prepareLotsList(it1: MutableList<VegaEcuadorDispatchStocks>): MutableList<VegaDispatchLots> {
    val lotList = mutableListOf<VegaDispatchLots>()
    it1.forEach { item ->
        val lot = VegaDispatchLots()
        lot.batchNumber = item.batchNumber
        lot.materialCode = item.materialCode
        lot.materialName = item.materialName
        lot.plantId = item.plantId
        lot.plantName = item.plantName
        lot.storageLocationCode = item.storageLocationCode
        lot.unitOfMeasure = item.unitOfMeasure
        lot.weight = item.weight
        lot.weight = item.weight
        lot.weight = item.weight
        lot.bagType = item.bagType
        lot.totalBagWeight = item.totalBagWeight
        lot.totalNoOfBags = item.totalNoOfBags
        lotList.add(lot)
    }
    return lotList
}

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getMaterialNameFromMaterialList(
    materialList: MutableList<VegaMaterial>,
    materialCode: String
): String? {
    val material = materialList.filter { materialCode.contains(it.materialCode) }
    return if (material.size > 0) material[0].materialName else ""
}

