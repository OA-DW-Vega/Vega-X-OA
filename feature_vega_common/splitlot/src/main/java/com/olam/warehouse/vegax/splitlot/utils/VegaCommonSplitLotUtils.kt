package com.olam.warehouse.vegax.splitlot.utils

import android.os.Build
import com.olam.warehouse.master.common.model.VegaCommonSplitLotModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCommonSplitMainModel

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */

const val SPLIT_LOT_ENTERED = "split_lot_enterd"
const val QUALITY_GRADE = "QUALITY_GRADE"
const val DANO = "DANO"
const val TYPE_T = "Ticket"
const val TYPE_R = "Receipt"
const val TYPE_SAMPLE = "Other"
const val BATCH_NO = "Batch_No"
const val GRN_NO = "Grn_No"
const val TICKET_KEY = "ticket_key"
const val PRINT_TYPE = "print_type"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun prepareSplitData(
    lotDetails: VegaCoffeeLot,
    splitLotListPost: ArrayList<VegaCommonSplitLotModel>
) : VegaCommonSplitMainModel {
    val splitModel = VegaCommonSplitMainModel()
    splitModel.batchNumber = lotDetails.batchNumber
    splitModel.storageLocationCode = lotDetails.storageLocationCode
    splitModel.netWeight = lotDetails.netWeight
    splitModel.unitsOfMeasure = lotDetails.unitsOfMeasure
    splitModel.materialCode = lotDetails.materialCode
    splitModel.materialName = lotDetails.materialName
    splitModel.weighBridgeId = lotDetails.weighBridgeId
    splitModel.key = getCurrentKey()
    splitModel.splitLots = splitLotListPost
    return splitModel
}
