package com.olam.warehouse.vegax.pileindiacoffee.ui.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

const val ADDLOT = "AddLot"
const val Lot_List = "LotList"
const val MODEL_BUNDLE = "model"
const val PILE_SELECT = "pileselect"
const val SHIFT = "Shift"
const val SUMMARY = "Summary"
const val FROM_SUMMARY = "fromSummary"
const val MULTIPLE_LOT = "addMultiple"
const val GRN_DATA = "GRN_DATA"
const val GRN_TYPE = "GRN_TYPE"
const val GRN_PTBF = "Grn_Ptbf"
const val GRN_SPOT = "Grn_Spot"
const val MATERIAL_LIST = "material_list"
const val EDIT_LOT = "edit_lot"
const val PILE_LIST = "pile_list"
const val INVENTORY_FRAG = "inventory_frag"
const val PILE_MANAGEMENT_SELECTION = "pile_management_selection"
const val PILE_MANAGEMENT_SUMMARY = "pile_management_summary"
const val PILE_MANAGEMENT_EDIT = "pile_management_edit"
const val FRAG_FILTER = "frag_filter"
const val FRAG_SUMMARY_BACK = "frag_summary_back"
const val MATERIAL_CODE = "material_code"
const val MATERIAL_NAME = "material_name"
const val IS_EDIT = "is_edit"
const val FULL_FILTER = "full_filter"
const val FILTER_WH_LOC = "filter_wh_loc"
const val FILTER_START_RANGE = "filter_start_range"
const val FILTER_END_RANGE = "filter_end_range"
const val FILTER_ABOVE_RANGE = "filter_above_range"
const val STORAGE_LOC = "storage_loc"
const val LOT_ID = "lot_id"
const val CREATE_NEW_LOT = "create_new_lot"
const val STOCK_LIST = "stock_list"
const val LOT_LIST = "lot_list"

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}
@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
