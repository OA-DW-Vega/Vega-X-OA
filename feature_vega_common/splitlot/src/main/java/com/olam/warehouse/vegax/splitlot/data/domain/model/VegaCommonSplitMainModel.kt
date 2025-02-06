package com.olam.warehouse.vegax.splitlot.data.domain.model

import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot

/**
 * Created by Baskaran Kannan on 9/28/2022.
 */


data class VegaCommonSplitMainModel(
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var storageLocationCode: String? = "",
    var netWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var key: String? = "",
    var plant: Plant? = getPlantDetails(),
    var splitLots : List<VegaCommonSplitLotModel> = emptyList()
)

data class VegaCoffeeQualityParamPost(
    var grnApplicable: Boolean,
    var grnFlag: Boolean,
    var key: String,
    var plant: Plant,
    var lotDetails: List<VegaCoffeeLot>? = emptyList(),
    var remarks: String?="",
    var isGain: Int = 0
)

data class VegaCoffeeQualityParamPostResponse(
    var grnApplicable: Boolean,
    var grnFlag: Boolean,
    var key: String,
    var plant: Plant,
    var lotQuality: List<VegaCoffeeLot>? = emptyList(),
    var remarks: String?="",
    var isGain: Int = 0
)

data class SplitUpdateTallySequencePost(
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var tallySheetSequenceValue:String? ="",
    var sequence: String? = "",         // Batch No Sequnce
    var isLotSequence: String? = "",    // Batch Sequnce flag
    var isInSequence: String? = "",     // Invoice Sequnce flag
    var invoiceSequence: String? = "",  // Invoice No Sequnce
    var grnSequence: String? = "", // Grn No Sequnce
    var isGrnRefSequence: String? = "",
    var poSequence: String? = "", // Po Sequnce flag
    var isPoRefSequence: String? = "",
    var isTallySheetSequence: String? = "",
    var isMTNRDocSequence: String? = "",
    var mtnrDocSequence: String? = "",
    var prefix3: String? = "",
    var key: String? = getCurrentKey()
)
