package com.olam.warehouse.vegax.offloading.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaOffloadingParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Baskaran Kannan on 2/1/2020.
 */

const val SUPPLIER = "Supplier"
const val WAREHOUSE = "Warehouse"
const val MTNR = "MTN-R"
const val PROCURE = "PROCURE"
const val STO = "STO"
const val OFFLOADING_DATA = "offloading_intent_data"
const val OFFLOADING_PARAM_DATA = "offloading_param_data"

const val PARAMS_LIST_FRAG = "params_list_frag"
const val SUMMARY_FRAG = "summary_frag"
const val DIRECTIONIN = "IN"


fun prepareData(quality1: List<VegaOffloadingParamsWithQualitative>): LiveData<List<VegaQualityParamsWithQualitative>> {
    val qualityParamList = arrayListOf<VegaQualityParamsWithQualitative>()
    val qualityList = MutableLiveData<List<VegaQualityParamsWithQualitative>>()
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            val qualitySortedBy = quality1.sortedBy { qual -> qual.offloadingParam.position }
            qualitySortedBy.forEach { quality ->
                val qualityParamsQualitative = VegaQualityParamsWithQualitative()
                val qualityParams = VegaQualityParameter()
                qualityParams.wbid = quality.offloadingParam.wbid
                qualityParams.wbTempId = quality.offloadingParam.wbTempId
                qualityParams.materialCode = quality.offloadingParam.materialCode
                qualityParams.descrChar = quality.offloadingParam.descrChar
                qualityParams.nameChar = quality.offloadingParam.nameChar
                qualityParams.entryObligatory = quality.offloadingParam.entryObligatory
                qualityParams.unitText = quality.offloadingParam.unitText
                qualityParams.dataType = quality.offloadingParam.dataType
                qualityParams.unitsOfMeasure = quality.offloadingParam.unitsOfMeasure
                qualityParams.numberDigits = quality.offloadingParam.numberDigits
                qualityParams.numberDecimals = quality.offloadingParam.numberDecimals
                qualityParams.numValFm = quality.offloadingParam.numValFm
                qualityParams.numValTo = quality.offloadingParam.numValTo
                qualityParams.currValFm = quality.offloadingParam.currValFm
                qualityParams.currValTo = quality.offloadingParam.currValTo
                qualityParams.valRelatn = quality.offloadingParam.valRelatn
                qualityParams.timeStamp = quality.offloadingParam.timeStamp
                qualityParams.qualityParameterValue = quality.offloadingParam.qualityParameterValue
                qualityParams.isSyncStatus = quality.offloadingParam.isSyncStatus
                qualityParamsQualitative.qualityParameter = qualityParams
                qualityParamsQualitative.qualitative = quality.qualitative
                qualityParamList.add(qualityParamsQualitative)
            }
            qualityList.value = qualityParamList
        }
    }

    return qualityList
}

fun prepareDOQualityData(data: VegaQualityParameter): VegaOffloadingParameter {
    val quality = VegaOffloadingParameter()
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
    quality.pre_sampling = data.preSampling
    quality.vaga_mandatory = data.vegaMandatory
    quality.quality_param_label = data.qualityParamLabel
    quality.unitsOfMeasure = data.unitsOfMeasure
    quality.formulaParam = data.formulaParam

    return quality
}


@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun sortDate(values: List<VegaOffloadingTrucks>): List<VegaOffloadingTrucks> {
    val vegaWbIds = values
    Collections.sort(vegaWbIds, object : Comparator<VegaOffloadingTrucks?> {
        @SuppressLint("SimpleDateFormat")
        override fun compare(o1: VegaOffloadingTrucks?, o2: VegaOffloadingTrucks?): Int {
            val format = SimpleDateFormat("dd-MM-yyyy")
            var compareResult = 0
            compareResult = try {
                val arg0Date = format.parse(dateFormat(o1?.erdat!!))
                val arg1Date = format.parse(dateFormat(o2?.erdat!!))
                arg0Date.compareTo(arg1Date)
            } catch (e: ParseException) {
                e.printStackTrace()
                dateFormat(o1?.erdat!!).compareTo(dateFormat(o2?.erdat!!))
            }
            return compareResult
        }
    })
    return vegaWbIds
}

fun dateFormat(value: String): String {
    val times = value.split('(', ')')
    return times.get(1).let { it1 ->
        DateUtils.getUTCDateTime(
            it1,
            App.getAppContext()
        )
    }
}
