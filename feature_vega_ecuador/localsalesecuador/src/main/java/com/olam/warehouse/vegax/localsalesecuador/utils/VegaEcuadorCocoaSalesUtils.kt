package com.olam.warehouse.vegax.localsalesecuador.utils

import android.graphics.drawable.Drawable
import android.os.Build
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.reflect.KMutableProperty1

const val SALES_TYPE_WEIGHBRIDGE = "Weighbridge"
const val SALES_TYPE_WEIGHSCALE = "Weighscale"
const val SALES_TYPE_ANTICIPATED = "Anticipated"
const val SALES_PENDING = "sales_pending"
const val WEIGHBRIDGE = "Bridge"
const val SCALE = "Scale"
const val UPDATE_WEIGHT = "UpdateWeight"
const val ADD_LOT = "Add_Lot"
const val SUMMARY = "Summary"
const val PENDING = "Pending"
const val SALES_ITEM = "sales_item"
const val SALES_TYPE = "sales_type"
const val DATE_FORMAT = "MM/dd/yyyy"
const val ADD_WEIGHT = "AddWeight"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val LOT_DETAIL = "lot_detail"
const val SEARCH_HINT = "Search Sales Order"
const val MATERIAL_LIST = "material_list"
const val MODEL_BUNDLE = "model_bundle"
const val INVENTORY_FRAG = "inventory_frag"
const val WEIGHBRIDGE_ADD_LOT = "weigh_bridge_add_lot"
const val MATERIAL_APPEND = "000000"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun getDrawable(id: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getDrawable(id, null)
    } else {
        App.getAppContext().resources.getDrawable(id)
    }
}

inline fun <reified T, Y> MutableList<T>.listOfField(property: KMutableProperty1<T, Y?>): MutableList<Y> {
    val yy = ArrayList<Y>()
    this.forEach { t: T ->
        yy.add(property.get(t) as Y)
    }
    return yy
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}


fun prepareCoffeBagMaterial(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoffeeSalesBagMaterial {
    val data = VegaCoffeeSalesBagMaterial()
    data.id = bagMaterial.id
    data.salesTempId = bagMaterial.message.toString()
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

fun prepareCocoaBagMaterial(bagMaterial: VegaCoffeeSalesBagMaterial): VegaCocoaSweepingBagMaterial {
    val data = VegaCocoaSweepingBagMaterial()
    data.id = bagMaterial.id
    data.message = bagMaterial.salesTempId
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
