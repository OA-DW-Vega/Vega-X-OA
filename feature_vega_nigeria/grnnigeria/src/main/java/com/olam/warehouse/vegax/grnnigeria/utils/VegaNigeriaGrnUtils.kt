package com.olam.warehouse.vegax.grnnigeria.utils

import android.os.Build
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.veganigeria.utils.GRN_PAID_PRICE
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGrnPostData
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGrnqualityPostData
import kotlin.random.Random

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */

const val GRN_FRAG = "grn_fragment"
const val GRN_OFFLINE_FRAG = "grn_offline_fragment"
const val GRN_WB_FRAG = "grn_wb_fragment"
const val GRN_DATA = "grn_data"
const val SPOT = "Spot"
const val FIXED = "Fixed"
const val GRN_QUALITY_DETAILS = "grn_quality_details"
const val QUALITY_DATA = "quality_data"
const val GRNT = "GRNT"
const val JUTE_BAG = "JUTE BAG"
const val GRN = "GRN"
const val GRN_PRICE = "grn_price"
const val PROCURE = "PROCURE"
const val JSON_RECEVING_LOCATION_LIST = "RECEVING_LOCATION"
const val RECEVING_PLANT_SLOC = "RECEVING_PLANT_SLOC"
const val JSON_PROCUREMENT_TYPE = "PROCUREMENT_TYPE"
const val GRNT_DATA = "grnt_data"
const val GRNT_PLANT_DETAILS = "grnt_plant_details"
const val MATERIAL_CODE = "000000"
const val SUPPLIER_CODE = "000"
const val SUMMARY_FRAG = "summary_frag"
const val RECEIVING_PLANT = "RECEIVING_PLANT"
const val UNIT_KG = "KG"
const val UNIT_Z01 = "Z01"
const val DD = "DD"
const val DX = "DX"
const val PR = "PR"
const val PX = "PX"
const val DR = "DR"
const val Z01 = "Z01"
const val GRN_VALUE = "101"

const val STO = "STO"
const val NGN = " NGN"

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun preparePostGrnData(
    wbDetails: VegaGrnWeighBridgeId,
    admixtureValue: String,
    bagTypeList: MutableList<VegaPackageMaterial>
): VegaNigeriaGrnPostData {
    var grnPost = VegaNigeriaGrnPostData()
    grnPost.batchNumber = wbDetails.batchNumber
    grnPost.item = wbDetails.item
    grnPost.materialCode = wbDetails.materialCode
    grnPost.netWeight = if (wbDetails.unitsOfMeasure.equals("KG")) convertKgToMT(
        wbDetails.netWeight.toString().trim()
    ) else wbDetails.netWeight.toString().trim()
    grnPost.plant = wbDetails.plantId
    //grnPost . price = if(wbDetails.unitsOfMeasure.equals("KG")) convertKgToMT(wbDetails.unitPrice.toString().trim()) else wbDetails.unitPrice.toString().trim()
    if(GRN_PAID_PRICE == "0") grnPost.price = wbDetails.unitPrice.toString().trim()
    else grnPost.price = wbDetails.totalPrice.toString().trim()
    grnPost.storageLocationCode = wbDetails.storageLocationCode
    grnPost.supplierCode = wbDetails.supplierCode
    //grnPost.unitsOfMeasure = wbDetails.unitsOfMeasure
    grnPost.unitsOfMeasure = "MT"
    grnPost.weighBridgeType = wbDetails.weighBridgeType
    grnPost.weighBridgeId = wbDetails.weighBridgeId
    grnPost.purchaseDocNum = wbDetails.purchaseDocNum
    grnPost.bagCount = wbDetails.bagCount
    grnPost.bagType = wbDetails.bagType
    grnPost.grnModel = wbDetails.grnModel
    grnPost.procurementType = wbDetails.procurementType
    grnPost.finalApproval = "X"
    grnPost.bagWeight = wbDetails.bagWeight
    var filteredBagTypeList = bagTypeList.filter { it.bagType == wbDetails.bagType?.trim() }
    if (filteredBagTypeList.size > 0) {
        grnPost.bagMaterialCode = filteredBagTypeList.get(0).bagMaterialCode
    }
    return grnPost
}


fun prepareQualityPostData(
    postData: ArrayList<VegaQualityParams>,
    grnPrice:String
): List<VegaQuality> {
    val grnPost = mutableListOf<VegaQuality>()

    postData.forEach {
        val item = VegaQuality()
        item.descrChar = it.qualityParameterName
        item.nameChar = it.sapQCName!!
        item.qualityParameterValue = it.satNam

        grnPost.add(item)
    }
    val quality3 = VegaQuality()
    quality3.descrChar = "GRN Price /MT (NE)"
    quality3.nameChar = "ZNGCOCOA_GRNPRICE"
    quality3.qualityParameterValue = grnPrice + NGN
    grnPost.add(quality3)

    return grnPost
}
fun prepareQualityPostData1(
    postData: ArrayList<VegaQualityParams>,
    postDataDB: List<VegaQualityParameter>,
    grnPrice:String,discount:String,dis_mould:String,dis_bean_weight:String,dis_bean_slay:String
): List<VegaNigeriaGrnqualityPostData?> {
    var grnPost =  mutableListOf<VegaNigeriaGrnqualityPostData>()
    postDataDB.forEach {
        val item = VegaNigeriaGrnqualityPostData()
        item.descrChar = it.descrChar
        postData.forEach { it1 ->
            when {
                it.descrChar.equals("GRN Price /MT (NE)") -> {
                    item.nameChar = "ZNGCOCOA_GRNPRICE"
                    item.qualityParameterValue = grnPrice + NGN
                }
                it.descrChar.equals("Discount on Other / MT") && it.nameChar.equals(it1.sapQCName) -> {
                    item.nameChar = it1.sapQCName!!
                    item.qualityParameterValue = discount + NGN
                }
                it.descrChar.equals("Mould Discount /MT") && it.nameChar.equals(it1.sapQCName)-> {
                    item.nameChar = it1.sapQCName!!
                    item.qualityParameterValue = dis_mould + NGN
                }
                it.descrChar.equals("Bean Weight Discount /MT") && it.nameChar.equals(it1.sapQCName)-> {
                    item.nameChar = it1.sapQCName!!
                    item.qualityParameterValue = dis_bean_weight + NGN
                }
                it.descrChar.equals("Bean Slaty Discount /MT") && it.nameChar.equals(it1.sapQCName)-> {
                    item.nameChar = it1.sapQCName!!
                    item.qualityParameterValue = dis_bean_slay + NGN
                }
                it.nameChar.equals(it1.sapQCName) -> {
                    item.nameChar = it1.sapQCName!!
                    item.qualityParameterValue = it1.satNam
                }
            }
        }
        grnPost.add(item)
    }
    return grnPost
}

fun preparePostGrnData1(
    postData: MutableList<VegaQualityParams>,
    postDataDB: List<VegaQualityParameter>
): List<VegaNigeriaGrnqualityPostData?> {
    val grnPost = mutableListOf<VegaNigeriaGrnqualityPostData>()
    postDataDB.forEach {
        val item = VegaNigeriaGrnqualityPostData()
        item.descrChar = it.descrChar
        postData.forEach { it1 ->
            if (it.nameChar.equals(it1.sapQCName)) {
                item.nameChar = it1.sapQCName
                item.qualityParameterValue = it1.satNam
            }
        }
        grnPost.add(item)
    }
    return grnPost
}

fun prepareWeighBridgeData(receiving: VegaReceiving): VegaGrnWeighBridgeId {
    val qualityWB = VegaGrnWeighBridgeId()
    qualityWB.weighBridgeId = receiving.tmpWbId
    qualityWB.wbTempId = receiving.tmpWbId
    qualityWB.weighBridgeType = receiving.weighBridgeType
    qualityWB.item = receiving.item
    qualityWB.direction = receiving.direction
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.batchNumber = receiving.charg
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (receiving.materialCode?.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = "000".plus(receiving.supplierCode)
    qualityWB.supplierName = receiving.supplierName
    qualityWB.bagCount = receiving.bagCount
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.posnr
    qualityWB.purchaseDocDesc = receiving.purchaseDocDesc
    qualityWB.purchaseDocNum = receiving.purchaseDocNum
    qualityWB.netWeight = receiving.netWeight
    qualityWB.grossWeight = receiving.grossWeight
    qualityWB.unitsOfMeasure = receiving.unitsOfMeasure
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

    return qualityWB
}

fun createRandomInteger(aStart: Int, aEnd: Long, aRandom: Random): Long {
    require(aStart <= aEnd) { "Start cannot exceed End." }
    //get the range, casting to long to avoid overflow problems
    val range = aEnd - aStart.toLong() + 1
    // compute a fraction of the range, 0 <= frac < range
    val fraction = (range * aRandom.nextDouble()).toLong()
    val randomNumber = fraction + aStart.toLong()
    return randomNumber
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

