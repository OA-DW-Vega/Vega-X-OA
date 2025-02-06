package com.olam.warehouse.vegax.thirdpartysalescoffee.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 8/31/2020.
 */


const val Ownership_Transfer = "ownership_transfer"
const val Third_Party_Olam = "third_party_olam"
const val Third_Party_Sales = "third_party_sales"
const val Third_Party_Sales_Summary = "third_party_sales_summary"
const val Ownership_Transfer_Summary = "ownership_transfer_summary"
const val Third_Party_Summary = "third_party_summary"
const val LOT_LIST = "LotFromInventory"
const val TP_TO_TP = "thirdPartyToThirdParty"
const val TP_TO_OLAM = "thirdPartyToOlam"
const val SAME_TP = "sameSales"
const val UPDATE_WEIGHT = "updateWeight"
const val ADD_WEIGHT = "add_weight"
const val WEIGHSCALE_ADD_LOT = "weigh_scale_add_lot"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val TP_WS = "thirdPartyWS"
const val TP_WB_WS = "thirdWBWS"

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
