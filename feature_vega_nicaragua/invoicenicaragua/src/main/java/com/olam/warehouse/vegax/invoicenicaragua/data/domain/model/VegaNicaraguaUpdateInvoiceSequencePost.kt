package com.olam.warehouse.vegax.invoicenicaragua.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaNicaraguaUpdateInvoiceSequencePost(
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var sequence: String? = "",
    var isLotSequence: String? = "",
    var isInSequence: String? = "",
    var invoiceSequence: String? = "",
    var grnSequence: String? = "",      // Grn No Sequnce
    var isGrnRefSequence: String? = ""  // Grn Sequnce flag
)
