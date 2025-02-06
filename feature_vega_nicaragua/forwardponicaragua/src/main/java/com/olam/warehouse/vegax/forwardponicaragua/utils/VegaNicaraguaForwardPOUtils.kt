package com.olam.warehouse.vegax.forwardponicaragua.utils

import android.os.Build
import com.olam.warehouse.vegax.App
import java.util.*
import kotlin.random.Random

/**
 * Created by Pavani  on 8/3/2020.
 */

const val FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG = "forward_po_creation_transaction_deatils_frag"
const val FORWARD_PO_CREATION_YIELD_DETAILS_FRAG = "forward_po_creation_yield_deatils_frag"
const val FORWARD_PO_CREATION_PRICING_DETAILS_FRAG = "forward_po_creation_pricing_deatils_frag"
const val FORWARD_PO_CREATION_PREVIEW_FRAG = "forward_po_creation_preview_frag"

fun getTmpId() = "TMP_".plus(Random.nextInt().toString())

fun covertToDouble(value1: String?): Double {
    var value = value1
    try{
        if(value?.startsWith(".") == true || value?.startsWith(",") == true) value = "0".plus(value)
        if (value != null && value.length > 0) {
            val str = value.format(Locale.ENGLISH ).replace(",", ".")
            return str.toDouble()
        }
    }catch (e:Exception){
        return 0.00
    }
    return 0.00
}

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}



