package com.olam.warehouse.vegax.grnecuador.utils

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

fun covertToDouble(value: String?): Double {
    if (value != null && value.length > 0) {
        val str = value.format(Locale.ENGLISH ).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}



