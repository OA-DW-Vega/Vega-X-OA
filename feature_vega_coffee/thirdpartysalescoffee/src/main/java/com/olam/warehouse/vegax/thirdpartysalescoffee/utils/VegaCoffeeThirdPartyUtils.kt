package com.olam.warehouse.vegax.thirdpartysalescoffee.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

/**
 * Created by Baskaran Kannan on 8/31/2020.
 */

const val MATERIAL_LIST = "material_list"
const val EDIT_LOT = "edit_lot"
const val WEIGHBRIDGE_LIST = "pile_list"
const val WEIGHBRIDGE_ADD_LOT = "weigh_bridge_add_lot"
const val DIRECTIONOUT = "OUT"
const val WEIGHBRIDGE = "weighbridge"
const val Ownership_Transfer = "ownership_transfer"
const val Third_Party_Olam = "third_party_olam"
const val Third_Party_Sales = "third_party_sales"
const val WEIGHBRIDGE_THIRD_PARTY_SUMMARY = "weighbridge_summary"
const val Third_Party_Sales_Summary = "third_party_sales_summary"
const val Ownership_Transfer_Summary = "ownership_transfer_summary"
const val Third_Party_Summary = "third_party_summary"
const val LOT_LIST = "LotFromInventory"
const val THIRD_PARTY_EDIT = "third_party_edit"
const val INVENTORY_FRAG = "inventory_frag"
const val TP_TO_TP = "thirdPartyToThirdParty"
const val TP_TO_OLAM = "thirdPartyToOlam"
const val SAME_TP = "sameSales"
const val UPDATE_WEIGHT = "updateWeight"
const val ADD_WEIGHT = "add_weight"
const val WEIGHSCALE_ADD_LOT = "weigh_scale_add_lot"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val TP_WS = "thirdPartyWS"
const val TP_WB_WS = "thirdWBWS"
const val MODEL_BUNDLE = "model"
const val VENDOR = "vendor"
const val MATERIAL = "material"
const val PO = "purchase_order"
const val PROCURE_TYPE = "procure_type"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}

fun getDrawable(id: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getDrawable(id, null)
    } else {
        App.getAppContext().resources.getDrawable(id)
    }
}

fun calculateStorageLoss(weight: String, editedWeight: String): String {
    val lossValue =
        (if (weight.isNotEmpty()) weight.toDouble() else 0.0).minus(if (editedWeight.isNotEmpty()) editedWeight.toDouble() else 0.0)
            .formatTwoDigits()
    return lossValue
}
