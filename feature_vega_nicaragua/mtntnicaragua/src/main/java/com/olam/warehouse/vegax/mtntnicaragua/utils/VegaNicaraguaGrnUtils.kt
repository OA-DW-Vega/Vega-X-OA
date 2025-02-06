package com.olam.warehouse.vegax.mtntnicaragua.utils

import android.os.Build
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.App
import java.util.*
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 11/12/2020.
 */
const val FRAG_ADD_LOT = "add_lot"
const val FRAG_LOT_LIST = "lot_list"
const val FRAG_SUMMARY = "lot_summary"
const val FRAG_CONSIGN = "lot_consign"
const val FRAG_ADD_WEIGHT = "add_weight"
const val FRAG_ADD_LOT_BACK = "add_lot_back"
const val FRAG_MERGE_LOT = "Merge_Lot"

const val UNITS_OF_MEASURE = "units_of_measure"
const val BAG_MATERIAL = "bag_material"
const val BAG_TYPE = "bag_type"
const val MATERIAL_NAME = "material_name"
const val SENDING_PLANT = "sending_plant"
const val STO_NO = "sto_number"
const val VENDOR = "vendor"
const val VENDORNAME = "vendor_name"
const val MATERIAL = "material"
const val CERTIFICATION = "certification"
const val BAG_COUNT = "bagcount"
const val QUALITY_GRADE = "quality_grade"
const val MTNT_WEIGHSCALE = "mtnt_weighscale"
const val MODEL_BUNDLE = "model_bundle"
const val EDIT_LOT = "edit_lot"
const val LOT_ID = "lot_id"
const val WEIGHSCALE = "weighscale"
const val QUALITY_PARAMS_DATA = "quality_params"
const val MTNR_Print = "mtnr_print"
var MERGED = false
var islotMERGED = false
var TICKET = "ticket"
var MILLING_PLANT=false
var THIRD_PARTY_PLANT=false
var DRYING_PLANT=false
var MTNR="MTNR"
var MODULENAME="MTNR"
var MODULENO="module_no"
var TRANSACTIONNO="trans_no"
var MTNR_RECEIPT= "Receipt"
var MTNR_Ticket= "Ticket"
var TYPE = "type"


fun getTmpId() = "TMP_".plus(Random.nextInt().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun covertToDouble(value: String?): Double {
    if (value != null && value.length > 0) {
        val str = value.format(Locale.ENGLISH).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}

fun prepareLotItems(
    selectedLots: List<VegaNicaraguaGRNInventoryDetails>,
    tmpId: String?
): ArrayList<VegaNicDispatchLots> {
    val nicLots = ArrayList<VegaNicDispatchLots>()
    selectedLots.forEach {
        val nicItem = VegaNicDispatchLots()
        nicItem.batchNumber = it.lotId
        nicItem.tempId = tmpId.toString()
        nicItem.tempIdWithBatch = tmpId.toString().plus(it.lotId)
        nicItem.materialCode = it.materialCode
        nicItem.materialName = it.materialName
        nicItem.qualityGrade = it.qualityGrade
        nicItem.certification = it.certification
        nicItem.plantId = it.plantId
        nicItem.storageLocationCode = it.procureLocationCode
        nicItem.unitOfMeasure = it.uom
        nicItem.weight = it.stockQty
        nicItem.editedWeight = it.editedWeight
        nicItem.isChecked = it.isChecked
        nicItem.vendorCode=it.vendorCode
        nicItem.vendorName=it.vendorName
        nicItem.availbagCount = it.availablebagCount.toString()
//        nicItem.processOrderNo = it.processOrderNo
//        nicItem.meins = it.meins
//        nicItem.rsNum = it.rsNum
//        nicItem.rsPos = it.rsPos
//        nicItem.bwart = it.bwart
//        nicItem.phase = it.phase
//        nicItem.deliveryItem = it.deliveryItem
//        nicItem.xchpf = it.xchpf
//        nicItem.isLowerWeight = it.isLowerWeight
//        nicItem.weightToDispatchUOM = it.weightToDispatchUOM
        nicItem.isEndLot = it.isEndLot
//        nicItem.storageLossFlag = it.storageLossFlag
        nicLots.add(nicItem)
    }
    return nicLots
}

fun prepareLotDataItems(it1: List<VegaNicDispatchLots>): List<VegaNicDispatchLots> {
    val lotList = arrayListOf<VegaNicDispatchLots>()
    it1.forEach {
        val dispatchLot = it.copy()
        val qualityList = it.materialQuality.qualityParameters
        qualityList.forEach { qty ->
            if (qty.sapQCName.equals("NIPOSITI"))
                dispatchLot.qualityGrade = qty.satNam
            else if (qty.sapQCName.equals("NIFG0014"))
                dispatchLot.certification = qty.satNam

        }
        lotList.add(dispatchLot)
    }
    return lotList
}
 fun calculateStorageLoss(weight: String, editedWeight: String): String {
    val lossValue =
        (if (weight.isNotEmpty()) weight.toDouble() else 0.0).minus(if (editedWeight.isNotEmpty()) editedWeight.toDouble() else 0.0)
            .formatTwoDigits()
    return lossValue
}

