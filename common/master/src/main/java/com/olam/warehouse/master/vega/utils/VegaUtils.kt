package com.olam.warehouse.master.vega.utils

import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative


fun prepareVegaQualityList(
    qualityParameterList: List<VegaQualityParameter?>,
    items: ArrayList<VegaQualityParamsWithQualitative?>
):List<VegaQuality> {
    val qualityList = arrayListOf<VegaQuality>()

    qualityParameterList.forEach { data->
        val quality = VegaQuality()
        quality.materialCode = data?.materialCode.toString()
        quality.descrChar = data?.descrChar
        quality.nameChar = data?.nameChar.toString()
        quality.qualityParameterValue = data?.qualityParameterValue
        quality.wbTempId = data?.wbTempId!!
        quality.wbid = data.wbid
        quality.currValFm = data.currValFm
        quality.currValTo = data.currValTo
        quality.dataType = data.dataType
        quality.entryObligatory = data.entryObligatory
        quality.numValFm = data.numValFm
        quality.numValTo = data.numValTo
        quality.isSyncStatus = data.isSyncStatus
        quality.numberDecimals = data.numberDecimals
        quality.numberDigits = data.numberDigits
        quality.unitText = data.unitText
        quality.unitsOfMeasure = data.unitsOfMeasure
        quality.valRelatn = data.valRelatn
        quality.timeStamp = data.timeStamp
        quality.status = data.status
        quality.syncStatusMsg = data.syncStatusMsg
        quality.preSampling = data.preSampling
        quality.vegaMandatory = data.vegaMandatory
        quality.qualityParamLabel = data.qualityParamLabel
        quality.formulaParam = data.formulaParam
        quality.position = data.position

        var qualityLists = items.filter {
            it?.qualityParameter?.nameChar?.contains(quality.nameChar.toString(), true)==true && it.qualitative?.isNotEmpty() == true}
        if (qualityLists.isNotEmpty()) {
            qualityLists.forEach {
                var quality1 = it?.qualitative?.filter { it.materialCode.contains(quality.materialCode.toString()) && data.qualityParameterValue?.equals(it.descValue, true)!! }
                quality1?.forEach {
                    quality.qualityParameterValue = it.charValue.trim()
                }
            }
        }
        qualityList.add(quality)

    }

    return qualityList
}
