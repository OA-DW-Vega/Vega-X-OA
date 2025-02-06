package com.olam.warehouse.vegax.bagissuenigeriacocoa.utils
import android.os.Build
import com.olam.warehouse.vegax.App
import java.util.*

const val ADD_CONTAINER = "add_container"
const val CONTAINER_INVENTORY = "container_inventory"
const val  NEW_CONTAINER = "New Container"
const val  COMPLETED_CONTAINER = "Completed"
const val  PRODUCT = "product"
const val  SUPPLIER = "supplier"

const val CONTAINER_DATA = "container_intent_data"
const val CONTAINER_DETAILS = "container_stuffing_intent_data"
const val STUFFING_DETAILS = "container_stuffed_lot_details_intent_data"
const val MATERIAL_CODE = "000000"
const val SUMMARY_FRAG = "summary_frag"
const val BAG_ISSUE_FRAG = "bag_issue_fragment"
const val BAG_RETN_FRAG = "bag_retn_fragment"
const val BAG_ISSUE_DATA = "bag_issue_intent_data"
const val SCREEN_TYPE = "screen_type"
var BATCHNUMBER="batchNumber"
var UOM="unitofMeasure"
@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getDiffenceBtwDates(date:String): String{
    val thatDay: Calendar = Calendar.getInstance()
    thatDay.set(Calendar.DAY_OF_MONTH,25)
    thatDay.set(Calendar.MONTH,7)
    thatDay.set(Calendar.YEAR, 1985)
    val today: Calendar = Calendar.getInstance()
    val diff = today.timeInMillis - thatDay.timeInMillis //result in millis

    return diff.toString()
}
