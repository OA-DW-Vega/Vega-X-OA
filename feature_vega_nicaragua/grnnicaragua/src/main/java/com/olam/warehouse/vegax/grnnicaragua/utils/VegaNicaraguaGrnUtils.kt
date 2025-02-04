package com.olam.warehouse.vegax.grnnicaragua.utils

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */

const val PROCUREMENT_TYPE = "weighment_type"
const val GRN_SPOT = "Grn_Spot"
const val GRN_FIXED = "Grn_Fixed"
const val GRN_PTBF = "Grn_Ptbf"
const val GRN_TOLLING = "Grn_Tolling"
const val GRN_WEIGHMENT = "Grn_Weighment"
const val FRAG_ADD_BAG_WEIGHT = "frag_add_bag_weight"
const val FRAG_PRICING = "frag_pricing"
const val FRAG_PTBF_PRICING = "frag_ptbf_pricing"
const val FRAG_SPOT_GRN_SUMMARY = "frag_spot_grn_summary"
const val FRAG_PTBF_GRN_SUMMARY = "frag_ptbf_grn_summary"
const val FRAG_SUMMARY = "frag_summary"
const val MATERIAL_CODE = "000000"
const val UNITS_OF_MEASURE = "units_of_measure"
const val BAG_MATERIAL = "bag_material"
const val BAG_TYPE = "bag_type"
const val MATERIAL_NAME = "material_name"
const val IS_PARAMS_VALUE = "is_params_values"
const val FNQUALITY = "Q"
const val GRN_QUALITY = "Grn_Quality"
const val GL_DETAILS = "GL_DETAILS"
const val GRN_TYPE = "GRN_TYPE"
const val GRN_DATA = "GRN_DATA"

fun getTmpId() = "TMP_".plus(Random.nextInt().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun sortByListOfItems(distinctByList: List<VegaQualityParamsWithQualitative>): List<VegaQualityParamsWithQualitative> {
    val sortedList = arrayListOf<VegaQualityParamsWithQualitative>()
    val value1 = distinctByList.filter { !it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value1.sortedBy { it.qualityParameter.priorityOrder?.toInt() })
    val value2 = distinctByList.filter { it.qualityParameter.priorityOrder.isNullOrEmpty() }
    sortedList.addAll(value2)
    return sortedList
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

fun preparaDataOffline(
    qty: List<VegaQuality>,
    qtyParams: List<VegaQualityParamsWithQualitative>
): MutableLiveData<List<VegaQualityParamsWithQualitative>> {
    var qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            qtyParams.forEach {
                qty.forEach { it1 ->
                    if (it.qualityParameter.nameChar.equals(it1.nameChar))
                        it.qualityParameter.qualityParameterValue = it1.qualityParameterValue
                }
            }
            qualityList.value = qtyParams
        }
    }
    return qualityList
}

fun prepareQualityData(data: VegaQualityParameter): VegaQuality {
    val quality = VegaQuality()
    quality.materialCode = data.materialCode
    quality.descrChar = data.descrChar
    quality.nameChar = data.nameChar
    quality.qualityParameterValue = data.qualityParameterValue
    quality.wbTempId = data.wbTempId!!
    quality.wbid = data.wbid
    quality.currValFm = data.currValFm
    quality.currValTo = data.currValTo
    quality.dataType = data.dataType
    quality.entryObligatory = data.entryObligatory
    quality.numValFm = data.numValFm
    quality.numValTo = data.numValTo
    quality.isSyncStatus = false
    quality.numberDecimals = data.numberDecimals
    quality.numberDigits = data.numberDigits
    return quality
}

fun covertToDouble(value: String?): Double {
    if (value != null && value.length > 0) {
        val str = value.format(Locale.ENGLISH ).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}

fun prepareQualityParamData(quality1: List<VegaQuality>): ArrayList<VegaQualityParameter?> {
    val qualityParamList = arrayListOf<VegaQualityParameter?>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.position }
            qualitySortedBy.forEach { quality ->
                val qualityParams = VegaQualityParameter()
                qualityParams.wbid = quality.wbid
                qualityParams.wbTempId = quality.wbTempId
                qualityParams.materialCode = quality.materialCode
                qualityParams.descrChar = quality.descrChar
                qualityParams.nameChar = quality.nameChar
                qualityParams.entryObligatory = quality.entryObligatory
                qualityParams.unitText = quality.unitText
                qualityParams.dataType = quality.dataType
                qualityParams.unitsOfMeasure = quality.unitsOfMeasure
                qualityParams.numberDigits = quality.numberDigits
                qualityParams.numberDecimals = quality.numberDecimals
                qualityParams.numValFm = quality.numValFm
                qualityParams.numValTo = quality.numValTo
                qualityParams.currValFm = quality.currValFm
                qualityParams.currValTo = quality.currValTo
                qualityParams.valRelatn = quality.valRelatn
                qualityParams.timeStamp = quality.timeStamp
                qualityParams.qualityParameterValue = quality.qualityParameterValue
                qualityParams.isSyncStatus = quality.isSyncStatus
                qualityParamList.add(qualityParams)
            }
        }
    }

    return qualityParamList
}

fun prepareAdvanceLineItem(
    selectedList: ArrayList<AdvanceLineItemDetails>,
    tmpWbId: String
): ArrayList<VegaNicaraguaAdvanceLineItemGrn> {
    val advanceList = arrayListOf<VegaNicaraguaAdvanceLineItemGrn>()
    selectedList.forEach { item ->
        val lineItem = VegaNicaraguaAdvanceLineItemGrn()
        lineItem.tmpWbId = tmpWbId
        lineItem.documentNumber = item.documentNumber.toString()
        lineItem.financialYear = item.financialYear
        lineItem.currency = item.currency
        lineItem.postingDate = item.postingDate
        lineItem.documentDate = item.documentDate
        lineItem.date = DateUtils.getCurrentTimeInMills().toString()
        lineItem.baselineDate = item.baselineDate
        lineItem.indicator = item.indicator
        lineItem.businessArea = item.businessArea
        lineItem.amount = item.amount
        lineItem.companyCode = item.companyCode
        lineItem.vendor = item.vendor
        lineItem.itemNum = item.itemNum
        lineItem.advanceKnockAmount = item.advanceKnockAmount
        lineItem.totalAdvanceKnockAmount = item.totalAdvanceKnockAmount
        lineItem.interestAmount = item.interestAmount
        lineItem.commissionAmount = item.commissionAmount
        lineItem.legalExpenseAmount = item.legalExpenseAmount
        lineItem.currencyDevaluationAmount = item.currencyDevaluationAmount
        advanceList.add(lineItem)
    }
    return advanceList
}

fun saveLotSequence(batchNumber: String) {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentBatch = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        val userIndicator = currentBatch.substring(0, 1)

        val lotSequence = currentBatch.substring(currentBatch.length - 5).toInt().inc()
        when (lotSequence.toString().length) {
            1 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("0000".plus(lotSequence.toString())))
            2 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("000".plus(lotSequence.toString())))
            3 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("00".plus(lotSequence.toString())))
            4 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("0".plus(lotSequence.toString())))
            5 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus(lotSequence.toString()))
        }
    }
}
