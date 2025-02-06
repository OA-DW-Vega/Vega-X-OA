package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import androidx.room.Ignore
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant

data class NicaraguaUpdateFgrnSequencePost (
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var sequence: String? = "",         // Batch No Sequnce
    var fgrnTicketSequenceValue: String? = "",         // Batch No Sequnce
    var fgrnSequenceValue: String? = "",         // Batch No Sequnce
    var isLotSequence: String? = "",    // Batch Sequnce flag
    var isInSequence: String? = "",     // Invoice Sequnce flag
    var invoiceSequence: String? = "",  // Invoice No Sequnce
    var grnSequence: String? = "", // Grn No Sequnce
    var isGrnRefSequence: String? = "",
    var poSequence: String? = "", // Po Sequnce flag
    var isPoRefSequence: String? = "",
    var isTallySheetSequence: String? = "",
    var isFGRNTicketSequence: String? = "",
    var isFGRNSequence: String? = "",
    var prefix3: String? = "",
    var key: String? = getCurrentKey(),
    var tallySheetSequenceValue:String?=""
)
