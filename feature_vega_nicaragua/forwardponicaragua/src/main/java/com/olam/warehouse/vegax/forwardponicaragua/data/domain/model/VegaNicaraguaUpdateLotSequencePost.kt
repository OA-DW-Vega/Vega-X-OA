package com.olam.warehouse.vegax.forwardponicaragua.data.domain.model

import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant

data class VegaNicaraguaUpdateLotSequencePost(
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var sequence: String? = "",         // Batch No Sequnce
    var isLotSequence: String? = "",    // Batch Sequnce flag
    var isInSequence: String? = "",     // Invoice Sequnce flag
    var invoiceSequence: String? = "",  // Invoice No Sequnce
    var grnSequence: String? = "",      // Grn No Sequnce
    var isGrnRefSequence: String? = "",
    var poSequence: String? = "" ,// Po Sequnce flag
    var isPoRefSequence: String? = "",
    var prefix3: String? = "",
    var key: String? = getCurrentKey()
)
