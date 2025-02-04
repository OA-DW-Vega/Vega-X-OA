package com.olam.warehouse.vegax.offloadingcameroon.utils

import android.os.Build
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
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
const val JUTE_BAG = "JUTE BAG"
const val POLY_BAG = "POLY BAG"
const val CI_JUTEBAG_CAM = "CI_JUTEBAG_CAM"
const val CI_NYLONBAG_CAM = "CI_NYLONBAG_CAM"
const val CI_NOPALLET_CAM = "CI_NOPALLET_CAM"
const val RECEIVING_PLANT = "RECEIVING_PLANT"
const val SOURCE_LOT = "SOURCE_LOT"
const val OFFLOADING_SUMMARY_FRAG = "offloading_summary_frag"
const val OFFLOADING_ADD_WEIGHT_FRAG = "offloading_add_weight_frag"
const val OFFLOADING_DATA = "offloading_intent_data"
const val OFFLOADING_PLANT_DETAILS_2 = "offloading_plant_details_intent_data"
const val OFFLOADING_POST_DATA = "offloading_intent_post_data"
const val OFFLOADING_POST_BAG_DATA = "offloading_intent_post_bag_data"
const val OFFLOADING_RECEIVING_PLANT = "offloading_intent_post_receiving_plant_batch_char"
const val OFFLOADING_OT_LOT_NUMBER = "offloading_intent_post_ot_lot_batch_char"
const val OFFLOADING_TRUCK_DATA = "offloading_intent_truck_data"
const val OFFLOADING_OFFLINE = "offloading_offline"
const val OFFLOADING_PLANT_SELECTED = "offloading_plant_selected"
const val OFFLOADING_PLANT_DETAILS = "offloading_plant_selected_details"


const val SUPPLIER = "supplier"
const val SUPPLIER_Caps = "Supplier"
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

const val DIRECTIONIN = "IN"
const val STO = "STO"
const val WAREHOUSE = "Warehouse"
const val PARAMS_LIST_FRAG = "params_list_frag"
const val TRUCK_LIST_FRAG = "truck_list_frag"


fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String?): String? {
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
    data.bagType1 = bagMaterial.bagType1
    data.bagCount = bagMaterial.bagCount
    data.bagCount1 = bagMaterial.bagCount1
    data.bagMaterialCode = bagMaterial.bagMaterialCode
    data.bagMaterialCode1 = bagMaterial.bagMaterialCode1
    data.tareWeight = bagMaterial.tareWeight
    data.tareWeight1 = bagMaterial.tareWeight1
    data.unitsOfMeasure = bagMaterial.unitsOfMeasure
    data.palletWeight = bagMaterial.palletWeight
    data.noOfPallet = bagMaterial.noOfPallet
    data.palletAverage = bagMaterial.palletAverage
    return data
}

fun prepareCameroonBagMaterial(bagMaterial: VegaCoffeeOffloadingBagMaterial): VegaCocoaSweepingBagMaterial {
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
 fun VegaEcuadorOffloadingBagMaterial.convertToVegaCameroonBagMaterial()= VegaCameroonOffloadingBagMaterial(
    id = id,
     wbId = wbId,
     batchNumber = batchNumber,
     netWeight = netWeight,
     tareWeight = tareWeight,
     palletAverage = palletAverage,
     palletWeight = palletWeight,
     noOfPallet = noOfPallet,
     grossWeight = grossWeight,
     bagCount = bagCount,
     bagType = bagType,
     unitsOfMeasure = unitsOfMeasure,
     purcheseOrderNo = purcheseOrderNo,
     purchaseDocDesc = purchaseDocDesc,
     supplierName = supplierName,
     materialName = materialName,
     truckNo = truckNo,
     mtntNo = mtntNo,
     mtntWeight = mtntWeight,
     receivingLocation = receivingLocation
 )
