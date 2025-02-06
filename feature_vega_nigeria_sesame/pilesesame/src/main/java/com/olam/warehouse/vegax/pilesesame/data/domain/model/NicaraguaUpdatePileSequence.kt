package com.olam.warehouse.vegax.pilesesame.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import kotlinx.android.parcel.Parcelize

data class NicaraguaUpdatePileSequence(
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var sequence: String? = "",         // Batch No Sequnce
    var tallySheetSequenceValue:String?="",
    var fgrnTicketSequenceValue: String? ="",
    var pileSequenceValue :String?="",
    var isPileSequence: String? = "",    // Batch Sequnce flag
    var isLotSequence: String? = "",    // Batch Sequnce flag
    var isInSequence: String? = "",     // Invoice Sequnce flag
    var invoiceSequence: String? = "",  // Invoice No Sequnce
    var grnSequence: String? = "", // Grn No Sequnce
    var isGrnRefSequence: String? = "",
    var poSequence: String? = "", // Po Sequnce flag
    var isPoRefSequence: String? = "",
    var isTallySheetSequence: String? = "",
    var isFGRNTicketSequence:String? ="",
    var prefix3: String? = "",
    var key: String? = getCurrentKey()
)

@Parcelize
data class PilePrintDetails(
    var pileNumber:String?="",
    var ticketNumber:String?="",
    var receivingDate:String?="",
    var gradeDetails:String?="",
    var gradeDesc:String?="",
    var certificate:String?="",
    var netWeight:String?="",
    var unitsOfMeasure:String?="",
    var storageLocation:String?=""
):Parcelable
