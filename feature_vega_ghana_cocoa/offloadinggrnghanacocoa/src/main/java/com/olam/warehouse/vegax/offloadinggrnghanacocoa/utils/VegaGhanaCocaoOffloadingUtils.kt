package com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.random.Random


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
const val GRN_PRICE = "offloading_intent_grn_price"
const val IS_OFFLINE = "IS_OFFLINE"
const val OFFLOADING_POST_DATA = "offloading_intent_post_data"
const val OFFLOADING_POST_BAG_DATA = "offloading_intent_post_bag_data"
const val OFFLOADING_GANA_DSC_GRN = "offfloading_gana_dsc_grn"
const val OFFLOADING_OFFLINE = "offloading_offline"
const val MTNR_OFFLOADING_OFFLINE = "mtnr_offloading_offline"
const val OFFLOADING_OFFLINE_SUMMARY_FRAG = "mtnr_offloading_offline_summary"
const val OFFLINE_DATA = "mtnr_offloading_offline_data"
const val JSON_COST_CENTRE_LIST = "COST_CENTRE_LIST"
const val JSON_REJECTED_MAPPING_LIST = "Rejected_Mapping"
const val JSON_RECEVING_LOCATION_LIST = "RECEVING_LOCATION"
const val UNIT_ZERO = "0.0"


const val SUPPLIER = "supplier"
const val VEGA_MODULE = "vegaModule"
const val DW_MODULE = "dwModule"
const val MODEL_BUNDLE = "model"
const val SELECTED_LOT_ID = "selectedLotId"
const val WEIGHBRIDGE_SUMMARY = "wb_summary"
const val MTNR = "mtnr"
const val LOT_LIST = "lot_list"
const val EXPORT_SALES = "export_sales"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val ADD_WEIGHT = "add_weight"
const val UPDATE_WEIGHT = "update_weight"
const val FRAG_ADD_BAG_WEIGHT_SUPPLIER = "add_bag_weight"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val MTNR_WEIGHBRIDGE_SUMMARY = "mtnr_wb_summary"
const val MTNR_WEIGHSCALE_OFFLINE_SUMMARY = "mtnr_offline_summary"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val MTNT = "mtnt"
const val BAG = "BAG"
const val EDIT_LOT = "edit_lot"
const val WEIGHBRIDGE = "weighbridge"
const val MTNT_WEIGHSCALE = "weighscale"
const val WAREHOUSE = "Warehouse"
const val COPIED_WBID = "copied_wbid"
const val WEIGHBRIDGE_LIST_TYPE = "weigh_bridge_list_type"
const val COPIED_MATERIAL = "copied_material"
const val STO = "STO"
const val WB_DATA = "wb_details"

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

fun prepareData(
    data: VegaCoffeeReceiving
): VegaQualityWBDetails {
    var wbList = VegaQualityWBDetails()
    wbList.wbTempId = data.tempWBId!!
    wbList.weighBridgeId = data.tempWBId!!
    wbList.weighBridgeType = "STO"
    wbList.direction = "IN"
    wbList.item = "00001"
    wbList.plant = data.plantId
    wbList.purchaseDocNum = data.purchaseDocNum
    wbList.batchNumber = data.batchNumber
    wbList.purchaseDocDesc = data.purchaseDocDesc
    wbList.salesDocNum = ""
    wbList.materialCode = data.ertim
    wbList.deliveryItem = "000010"
    wbList.delivery = data.delivery
    wbList.materialName = data.materialName
    wbList.netWeight = data.netWeight
    wbList.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
    wbList.bagWeight = data.bagWeight
    wbList.bagCount = data.bagCount
    wbList.bagType = data.bagType
    wbList.grossWeight = data.grossWeight
    wbList.unitsOfMeasure = data.unitsOfMeasure
    wbList.vehicleNumber = data.contactNumber
    wbList.storageLocationCode = data.storageLocationCode!!
    wbList.truckDriverName = data.truckDriverName
    wbList.driverName = data.driverName
    wbList.weighMethod = "WS"
    return wbList
}
