package com.olam.warehouse.vegax.offloadingcoffee.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

const val SUPPLIER = "supplier"
const val MTNR = "mtnr"
const val EXPORT_SALES = "export_sales"
const val WEIGHBRIDGE_WEIHSCALE = "weighbridge-weighscale"
const val WEIGHSCALE = "weighscale"
const val ADD_WEIGHT = "add_weight"
const val ADD_WEIGHT_SUPPLIER = "add_weight_supplier"
const val UPDATE_WEIGHT = "update_weight"
const val FRAG_ADD_BAG_WEIGHT = "add_bag_weight"
const val MTNR_WEIGHSCALE_SUMMARY = "mtnr_summary"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"
const val MTNT = "mtnt"
const val EDIT_LOT = "edit_lot"
const val OFFLOAD_DATA = "bundle_data"
const val PROCURE = "PROCURE"
const val WAREHOUSE = "Warehouse"
const val STO = "STO"
const val WEIGHSCALE_LIST = "weighscaleList"
const val MISC = "MISC"
const val WEIGHSCALE_SUPPLIER = "weighscale_supplier"
const val MATERIAL_APPEND = "000000"
const val SUPPLIER_APPEND = "000"
const val WB01 = "WB01"
const val WS01 = "WS01"
const val PTBF = "Z001"
const val MATERIAL = "material"
const val LOT_ID = "lot_id"
const val CERTIFICATION = "certification"
const val QUALITY_GRADE = "quality_grade"
const val QUALITY_PARAMS_DATA = "quality_params"
var grade: String? = "quality"
var certification: String? = "certificate"

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

fun prepareCoffeeBagMaterial(bagMaterial: VegaCoffeeOffloadingBagMaterial): VegaCocoaSweepingBagMaterial {
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
fun prepareData(quality1: List<VegaQualityWithQualitative>): LiveData<List<VegaQualityParamsWithQualitative>> {
    val qualityParamList = arrayListOf<VegaQualityParamsWithQualitative>()
    val qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.quality.position }
            qualitySortedBy.forEach { quality ->
                val qualityParamsQualitative = VegaQualityParamsWithQualitative()
                val qualityParams = VegaQualityParameter()
                qualityParams.wbid = quality.quality.wbid
                qualityParams.wbTempId = quality.quality.wbTempId
                qualityParams.materialCode = quality.quality.materialCode
                qualityParams.descrChar = quality.quality.descrChar
                qualityParams.nameChar = quality.quality.nameChar
                qualityParams.entryObligatory = quality.quality.entryObligatory
                qualityParams.unitText = quality.quality.unitText
                qualityParams.dataType = quality.quality.dataType
                qualityParams.unitsOfMeasure = quality.quality.unitsOfMeasure
                qualityParams.numberDigits = quality.quality.numberDigits
                qualityParams.numberDecimals = quality.quality.numberDecimals
                qualityParams.numValFm = quality.quality.numValFm
                qualityParams.numValTo = quality.quality.numValTo
                qualityParams.currValFm = quality.quality.currValFm
                qualityParams.currValTo = quality.quality.currValTo
                qualityParams.valRelatn = quality.quality.valRelatn
                qualityParams.timeStamp = quality.quality.timeStamp
                qualityParams.preSampling = quality.quality.preSampling
                qualityParams.vegaMandatory = quality.quality.vegaMandatory
                qualityParams.qualityParamLabel = quality.quality.qualityParamLabel
                qualityParams.formulaParam = quality.quality.formulaParam
                qualityParams.qualityParameterValue = quality.quality.qualityParameterValue
                qualityParams.isSyncStatus = quality.quality.isSyncStatus
                qualityParamsQualitative.qualityParameter = qualityParams
                qualityParamsQualitative.qualitative = quality.qualitative
                qualityParamList.add(qualityParamsQualitative)
            }
            qualityList.value = qualityParamList
        }
    }

    return qualityList
}
