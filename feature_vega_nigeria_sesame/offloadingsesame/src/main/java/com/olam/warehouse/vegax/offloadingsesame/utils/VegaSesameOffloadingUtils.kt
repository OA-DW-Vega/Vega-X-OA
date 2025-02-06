package com.olam.warehouse.vegax.offloadingsesame.utils

import android.os.Build
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */

const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val MATERIAL_CODE = "000000"
const val UNITS_OF_MEASURE = "units_of_measure"
const val BAG_MATERIAL = "bag_material"
const val MATERIAL_NAME = "material_name"
const val WS01 = "WS01"
const val PROCURE = "PROCURE"
const val OFFLOADING_SUMMARY_FRAG = "offloading_summary_frag"
const val OFFLOADING_ADD_WEIGHT_FRAG = "offloading_add_weight_frag"
const val OFFLOADING_DATA = "offloading_intent_data"
const val OFFLOADING_POST_DATA = "offloading_intent_post_data"
const val OFFLOADING_POST_BAG_DATA = "offloading_intent_post_bag_data"
const val OFFLOADING_OFFLINE = "offloading_offline"


const val SUPPLIER = "supplier"
const val MTNR = "mtnr"
const val EXPORT_SALES = "export_sales"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val ADD_WEIGHT = "add_weight"
const val UPDATE_WEIGHT = "update_weight"
const val FRAG_ADD_BAG_WEIGHT_SUPPLIER = "add_bag_weight"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val MTNT = "mtnt"
const val EDIT_LOT = "edit_lot"
const val ZERO_VALUE = "0.0"
const val PRODUCT = "product"
const val RECEIVING_WH = "receiving_warehouse"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun convertMtToKg(weight: String, uom: String): String {
    if (uom.equals("kg", true)) return weight
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String?, uom: String): String? {
    if (uom.equals("kg", true)) return weight
    return weight?.toDouble()?.div(1000)?.formatThreeDigits()
}

fun prepareItem(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeOffloadingBagMaterial{
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

fun prepareSesameBagMaterial(bagMaterial: VegaCoffeeOffloadingBagMaterial): VegaCocoaSweepingBagMaterial {
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
