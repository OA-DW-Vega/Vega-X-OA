package com.olam.warehouse.vegax.offloadingnigeria.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */

const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val FRAG_ADD_BAG_WEIGHT_NEW = "frag_add_bag_weight_new"
const val PARAMS_LIST_FRAG = "param_list_frag"
const val MATERIAL_CODE = "000000"
const val UNITS_OF_MEASURE = "units_of_measure"
const val BAG_MATERIAL = "bag_material"
const val MATERIAL_NAME = "material_name"
const val WS01 = "WS01"
const val PROCURE = "PROCURE"
const val OFFLOADING_LIST_DATA = "offloading_list_intent_data"
const val OFFLOADING_SUMMARY_FRAG = "offloading_summary_frag"
const val OFFLOADING_ADD_WEIGHT_FRAG = "offloading_add_weight_frag"
const val OFFLOADING_DATA = "offloading_intent_data"
const val OFFLOADING_TRUCK_DATA = "offloading_truck_intent_data"
const val OFFLOADING_POST_DATA = "offloading_intent_post_data"
const val OFFLOADING_POST_BAG_DATA = "offloading_intent_post_bag_data"
const val OFFLOADING_OFFLINE = "offloading_offline"
const val DIRECTIONIN = "IN"
const val ORGTYPE = "NG10"
const val STO = "STO"
const val SUPPLIER = "supplier"
const val MTNR = "mtnr"
const val ADD_WEIGHT = "add_weight"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val EDIT_LOT = "edit_lot"
const val UNIT_KG = "KG"
const val UNIT_MT = "MT"
const val ZERO_VALUE = "0.0"
const val UPDATE_WEIGHT = "update_weight"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun convertMtToKg(weight: String?, uom: String): String? {
    if (uom.equals("kg", true)) return weight
    val converted = weight?.toDouble()?.times(1000)
    return converted?.formatThreeDigits()
}

fun convertKgToMT(weight: String?, uom: String): String? {
    if (uom.equals("kg", true)) return weight
    return weight?.toDouble()?.div(1000)?.formatThreeDigits()
}

fun prepareNigeriaBagMaterial(bagMaterial: VegaCoffeeOffloadingBagMaterial): VegaCocoaSweepingBagMaterial {
    val data = VegaCocoaSweepingBagMaterial()
    data.id = bagMaterial.id
    data.message = bagMaterial.mtnNumber
    data.batchNumber = bagMaterial.batchNumber
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight
    data.bagType = bagMaterial.bagType
    data.bagCount = bagMaterial.bagCount
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.tareWeight = bagMaterial.tareWeight
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.palletAverage = bagMaterial.palletAverage
    return data
}

fun prepareQcWBList(weighBridge: List<VegaQualityWBDetails>): List<VegaQualityApproveCameroonWeighBridge> {
    var list = arrayListOf<VegaQualityApproveCameroonWeighBridge>()
    weighBridge.forEach {
        var item = VegaQualityApproveCameroonWeighBridge()
        item.batchNumber = it.batchNumber
        item.wbid = it.weighBridgeId
        item.charg = it.batchNumber
        item.finalApproval = it.finalApproval
        item.grnNumber = it.grnNumber
        item.werks = it.plant
        item.qualityDetails = it.qualityDetails
        item.materialName = it.materialName
        item.materialNumber = it.materialCode
        item.supplierCode = it.supplierCode
        item.supplierName = it.supplierName
        item.grnQty = it.netWeight
        item.qchar = it.qcStatus
        list.add(item)
    }
    return list
}

fun prepareItem(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeOffloadingBagMaterial {
    val data = VegaCoffeeOffloadingBagMaterial()
    data.id = bagMaterial.id
    data.mtnNumber = bagMaterial.message.toString()
    data.batchNumber = bagMaterial.batchNumber
    data.grossWeight = bagMaterial.grossWeight
    data.netWeight = bagMaterial.netWeight
    data.bagType = bagMaterial.bagType
    data.bagCount = bagMaterial.bagCount
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.tareWeight = bagMaterial.tareWeight
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.palletAverage = bagMaterial.palletAverage
    return data
}
