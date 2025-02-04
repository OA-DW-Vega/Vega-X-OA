package com.olam.warehouse.vegax.offloadingcocoa.utils

import android.os.Build
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlin.random.Random

const val SUPPLIER = "supplier"
const val MTNR = "mtnr"
const val EXPORT_SALES = "export_sales"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val ADD_WEIGHT = "add_weight"
const val UPDATE_WEIGHT = "update_weight"
const val FRAG_ADD_BAG_WEIGHT = "add_bag_weight"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val MTNT = "mtnt"
const val EDIT_LOT = "edit_lot"
const val TRANSACTION_SUMMARY = "transactionSummary"


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

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}

fun prepareItem(bagMaterial: VegaCocoaSweepingBagMaterial): VegaCoCoaOffloadingBagMaterial {
    val data = VegaCoCoaOffloadingBagMaterial()
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
    return data
}

fun prepareCoffeeBagMaterial(bagMaterial: VegaCoCoaOffloadingBagMaterial): VegaCocoaSweepingBagMaterial {
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

    return data
}
