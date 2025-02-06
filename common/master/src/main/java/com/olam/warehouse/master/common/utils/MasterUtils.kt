package com.olam.warehouse.master.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.olam.warehouse.master.common.model.OfflineInventory
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Key
import com.olam.warehouse.master.user.model.NotifyModuleList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.user.model.ThirdPartyMaterialItem
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.master.vegaghana.model.VegaGhanaLotQuality
import com.olam.warehouse.master.vegaghana.model.VegaGhanaMtnBatchNumber
import com.olam.warehouse.master.vegaghana.model.VegaGhanaRminBom
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails
import com.olam.warehouse.presentation.data.domain.model.VehicleData
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.fromJson
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */

// DOIntegration

fun prepareDOPackingMaterial(packageMaterial: List<PackageMaterial>): List<DOPackageMaterial> {
    val doPackageMaterial = arrayListOf<DOPackageMaterial>()
    packageMaterial.forEach {
        val packageMaterial = DOPackageMaterial()
        packageMaterial.bagType = it.bagType
        packageMaterial.standardWeight = it.standardWeight
        packageMaterial.tareWeight = it.tareWeight
        packageMaterial.unitsOfMeasure = it.unitsOfMeasure
        packageMaterial.plant = it.plant
        packageMaterial.tolerance = it.tolerance
        packageMaterial.isDefault = it.isDefault
        doPackageMaterial.add(packageMaterial)
    }
    return doPackageMaterial
}

fun prepareDOStorageLocation(storageLocation: List<StorageLocation>): List<DOStorageLocation> {
    val doStorageLocation = arrayListOf<DOStorageLocation>()
    storageLocation.forEach {
        val storageLocation = DOStorageLocation()
        storageLocation.storageLocationName = it.storageLocationName
        storageLocation.storageLocationCode = it.storageLocationCode
        storageLocation.plant = it.plant
        doStorageLocation.add(storageLocation)
    }
    return doStorageLocation
}

fun prepareDOSapMaterial(sapMaterial: List<Material>): List<DOMaterial> {
    val doMaterialList = arrayListOf<DOMaterial>()
    sapMaterial.forEach {
        val doMaterial = DOMaterial()
        doMaterial.currency = it.currency
        doMaterial.materialName = it.materialName
        doMaterial.materialCode = it.materialCode
        doMaterial.price = it.price
        doMaterial.plant = it.plant
        doMaterial.unitsOfMeasure = it.unitsOfMeasure
        doMaterial.languageCode = it.languageCode
//        doMaterial.isBltEnabled = it.isBltEnabled
        doMaterial.bltEnabled = it.bltEnabled
        doMaterial.scanLevelId = it.scanLevelId
        doMaterial.scanLevelName = it.scanLevelName
        doMaterial.scanLevelName = it.scanLevelName

        doMaterialList.add(doMaterial)
    }
    return doMaterialList
}

fun prepareDOVendor(vendors: List<Vendor>): List<DOVendor> {
    val doVendorList = arrayListOf<DOVendor>()
    vendors.forEach {
        val doVendor = DOVendor()
        doVendor.countryCode = it.countryCode
        doVendor.vendorAddress = it.vendorAddress
        doVendor.vendorCity = it.vendorCity
        doVendor.vendorCode = it.vendorCode
        doVendor.vendorName = it.vendorName
        doVendorList.add(doVendor)
    }
    return doVendorList
}

fun prepareDOWarehouse(warehouse: List<Warehouse>): List<DOWarehouse> {
    val doWarehouseList = arrayListOf<DOWarehouse>()
    warehouse.forEach {
        val doWarehouse = DOWarehouse()
        doWarehouse.plantId = it.plantId
        doWarehouse.warehouseId = it.warehouseId
        doWarehouse.warehouseName = it.warehouseName
        doWarehouseList.add(doWarehouse)
    }
    return doWarehouseList
}

fun prepareDOQualityParams(qualityParameter: List<QualityParameter>): List<DOQualityParameter> {
    val doQualityParameterList = arrayListOf<DOQualityParameter>()
    qualityParameter.forEach {
        val doQualityParameter = DOQualityParameter()
        doQualityParameter.wbid = it.wbid
        doQualityParameter.wbTempId = it.wbTempId
        doQualityParameter.materialCode = it.materialCode
        doQualityParameter.descrChar = it.descrChar
        doQualityParameter.nameChar = it.nameChar
        doQualityParameter.entryObligatory = it.entryObligatory ?: ""
        doQualityParameter.unitText = it.unitText
        doQualityParameter.dataType = it.dataType
        doQualityParameter.unitsOfMeasure = it.unitsOfMeasure
        doQualityParameter.numberDigits = it.numberDigits
        doQualityParameter.numberDecimals = it.numberDecimals
        doQualityParameter.numValFm = it.numValFm
        doQualityParameter.numValTo = it.numValTo
        doQualityParameter.currValFm = it.currValFm
        doQualityParameter.currValTo = it.currValTo
        doQualityParameter.valRelatn = it.valRelatn
        doQualityParameter.timeStamp = it.timeStamp
        doQualityParameter.qualityParameterValue = it.qualityParameterValue
        doQualityParameter.isSyncStatus = it.isSyncStatus
        doQualityParameter.position = it.position
        doQualityParameter.priorityOrder = it.priorityOrder
        doQualityParameter.mandatory = it.mandatory
        doQualityParameter.singleValue = it.singleValue
        doQualityParameter.doMandatory = it.doMandatory ?: ""
        doQualityParameter.formulaParam = it.formulaParam ?: ""
        doQualityParameter.appFormula = it.appFormula ?: ""
        doQualityParameter.parameterUsage = it.parameterUsage ?: ""
        doQualityParameterList.add(doQualityParameter)
    }

    return doQualityParameterList
}

fun prepareDOQualitative(data: Qualitative): DOQualitative {
    val doQualitative = DOQualitative()
    doQualitative.charValue = data.charValue
    doQualitative.descValue = data.descValue
    doQualitative.materialCode = data.materialCode
    doQualitative.nameChar = data.nameChar
    return doQualitative
}

fun getPlantDetails(): Plant {
    try {
        val plantDetailJson = PreferenceHelper.get(Constants.PLANT_DETAILS, "")
        return Gson().fromJson<Plant>(plantDetailJson)
    } catch (e: NullPointerException) {
        return Plant()
    }
}

fun getMultiPlantList(): List<Plant> {
    val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
    return plants
}

fun getNotifyModuleList(): List<NotifyModuleList>{
    val notifyModuleList = Gson().fromJson<List<NotifyModuleList>>(PreferenceHelper.get(Constants.NOTIFIY_MODULE_LIST, ""))
    return notifyModuleList
}

fun generateGrnReferenceNo(batchNo: String): String {
    var grnRefId = ""
    if (batchNo.isNotEmpty()) {
        val plant = getPlantDetails()
        val plantCode = plant.plantCode
        val plantId = plant.plantId
        val spBatch = batchNo.substring(2)
        grnRefId = plantId.plus(spBatch)
    }
    return grnRefId
}

fun validateInvoiceNoIsAlreadyExist(transGrnList: MutableList<VegaReceiving>, transInvoiceList: MutableList<VegaNicaraguaInvoiceDetails>, invoice: String): Boolean{
    val grnTrans = transGrnList.filter { (it.status.equals(Status.SYNC_PENDING) || it.status.equals(Status.SYNC_COMPLETED)) && it.invoiceNumber?.contains("TEM") == false }.map { it.invoiceNumber }
    val invoiceTrans = transInvoiceList.filter { it.isSynced || !it.isSynced }.map { it.invoiceNo }
    var transList = arrayListOf<String?>()
    transList.addAll(grnTrans)
    transList.addAll(invoiceTrans)
    val isSeqExist = transList.any { it?.contains(invoice) == true }
    if(isSeqExist){
        saveInvoiceSequence(transGrnList, true, transInvoiceList, true)
    }
    return isSeqExist
}

fun generateInvoiceRefNumber(transGrnList: MutableList<VegaReceiving>, transInvoiceList: MutableList<VegaNicaraguaInvoiceDetails>): String {
    val grnTrans = transGrnList.filter { (it.status.equals(Status.SYNC_PENDING) || it.status.equals(Status.SYNC_COMPLETED)) && it.invoiceNumber?.contains("TEM") == false }.map { it.invoiceNumber }
    val invoiceTrans = transInvoiceList.filter { it.isSynced || !it.isSynced }.map { it.invoiceNo }
    var transList = arrayListOf<String?>()
    transList.addAll(grnTrans)
    transList.addAll(invoiceTrans)
    var invoiceRefId = ""
    var year =""
    val plant = getPlantDetails()
    val plantCode = plant.plantCode
    val plantId = plant.plantId
    val rightNow = Calendar.getInstance()
   // val year = rightNow.get(Calendar.YEAR).toString().substring(2)
    if (!getCurrentKey().split("_")[1].contains("NI"))
        year = rightNow.get(Calendar.YEAR).toString().substring(2)
    else
    {
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
    }
    val invoiceSequence = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
//    val currentId = invoiceSequence.substring(invoiceSequence.length - 5)
  /*  val isSeqExist = transList.any { it?.contains(invoiceSequence) == true }
    if(isSeqExist){
        saveInvoiceSequence(transGrnList, true, transInvoiceList, true)
        return generateInvoiceRefNumberAgain(transGrnList, transInvoiceList)
    }*/
    invoiceRefId = plantId.plus(year).plus(invoiceSequence)

    return invoiceRefId
}

fun generateInvoiceRefNumberAgain(transGrnList: MutableList<VegaReceiving>, transInvoiceList: MutableList<VegaNicaraguaInvoiceDetails>): String {
    /*val grnTrans = transGrnList.filter { (it.status.equals(Status.SYNC_PENDING) || it.status.equals(Status.SYNC_COMPLETED)) && it.invoiceNumber?.contains("TEM") == false }.map { it.invoiceNumber }
    val invoiceTrans = transInvoiceList.filter { it.isSynced || !it.isSynced }.map { it.invoiceNo }
    var transList = arrayListOf<String?>()
    transList.addAll(grnTrans)
    transList.addAll(invoiceTrans)*/
    var invoiceRefId = ""
    var year =""
    val plant = getPlantDetails()
    val plantCode = plant.plantCode
    val plantId = plant.plantId
    val rightNow = Calendar.getInstance()
    // val year = rightNow.get(Calendar.YEAR).toString().substring(2)
    if (!getCurrentKey().split("_")[1].contains("NI"))
        year = rightNow.get(Calendar.YEAR).toString().substring(2)
    else
    {
        var currentmonth = (rightNow.get(Calendar.MONTH)+1).toString()
        var  currentyear = rightNow.get(Calendar.YEAR)
        if (PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").isEmpty()) {
            year = if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12"))
                (currentyear + 1).toString().substring(2) else currentyear.toString().substring(2)
        } else {
            year = PreferenceHelper.get(Constants.CROP_FINANCIAL_YEAR, "").takeLast(2)
        }
//        year = if(currentmonth.equals("10") || currentmonth.equals("11") ||currentmonth.equals("12"))
//            (currentyear+1).toString().substring(2) else currentyear.toString().substring(2)
    }
    val invoiceSequence = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
//    val currentId = invoiceSequence.substring(invoiceSequence.length - 5)
    /*val isSeqExist = transList.any { it?.contains(invoiceSequence) == true }
    if(isSeqExist){
        saveInvoiceSequence(transGrnList, true, transInvoiceList)
        generateInvoiceRefNumber(transGrnList, transInvoiceList)
    }*/
    invoiceRefId = plantId.plus(year).plus(invoiceSequence)

    return invoiceRefId
}

fun saveInvoiceSequence(transactionList: MutableList<VegaReceiving>, isOnline: Boolean, transInvoiceList: MutableList<VegaNicaraguaInvoiceDetails>, isDoubleIncre: Boolean) {
    val transGrnInvoiceNos =
        try {
            transactionList.filter {
                (it.status.equals(Status.SYNC_COMPLETED) || it.status.equals(Status.SYNC_PENDING)) && it.invoiceNumber?.contains("TEM") == false && !it.invoiceNumber.isNullOrEmpty()
            }.map { if(it.invoiceNumber?.isNotEmpty() == true)it.invoiceNumber?.takeLast(5)?.toInt()?:0 else 0 }
        } catch (e: StringIndexOutOfBoundsException) {
            emptyList()
        }catch (e:Exception){
            emptyList()
        }
    val transInvoiceNos =
        try {
            transInvoiceList.filter { ((it.isSynced)||(!it.isSynced)) && !it.invoiceNo.isNullOrEmpty() }.map { if(it.invoiceNo?.isNotEmpty() == true)it.invoiceNo?.takeLast(5)?.toInt()?:0 else 0 }
        }catch (e:StringIndexOutOfBoundsException){
            emptyList()
        }catch (e:Exception){
            emptyList()
        }

    var fullTransList = arrayListOf<Int>()
    fullTransList.addAll(transGrnInvoiceNos.filter { it!=0 })
    fullTransList.addAll(transInvoiceNos.filter { it!=0 })
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    //if (!isEditTrans) {
        val currentBatch = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
        if (currentBatch.isNotEmpty()) {
            val userIndicator = currentBatch.substring(0, 1)
            val lot = currentBatch.substring(currentBatch.length - 5).toInt()
            var lotSequence = currentBatch.substring(currentBatch.length - 5).toInt().inc()
            /*if(fullTransList.isNotEmpty()){
                var maxValue = lotSequence
                if(AppUtils.isOnline())
                    maxValue = fullTransList.maxByOrNull { it}?.inc()?:lotSequence
                else{
                    if(isDoubleIncre)
                        maxValue = (fullTransList.maxByOrNull { it }?.inc()?:lotSequence).inc()
                    else
                        maxValue = (fullTransList.maxByOrNull { it }?.inc()?:lotSequence)*//*.inc()*//*
                }
                when{
                    lotSequence != maxValue -> {
                        var diff = maxValue.minus(lotSequence).toString().toInt()
                        lotSequence = when{
                           diff<0 -> maxValue
                           diff>1 -> lotSequence
                            else -> maxValue

                       }

                    }
                }
                *//*if(fullTransList.contains(lotSequence)){
                    lotSequence = lotSequence.inc()
                }*//*
            }*/

            when (lotSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.INVOICE_SEQUENCE,
                    userIndicator.plus("0000".plus(lotSequence.toString()))
                )
                2 -> PreferenceHelper.save(
                    Constants.INVOICE_SEQUENCE,
                    userIndicator.plus("000".plus(lotSequence.toString()))
                )
                3 -> PreferenceHelper.save(
                    Constants.INVOICE_SEQUENCE,
                    userIndicator.plus("00".plus(lotSequence.toString()))
                )
                4 -> PreferenceHelper.save(
                    Constants.INVOICE_SEQUENCE,
                    userIndicator.plus("0".plus(lotSequence.toString()))
                )
                5 -> PreferenceHelper.save(
                    Constants.INVOICE_SEQUENCE,
                    userIndicator.plus(lotSequence.toString())
                )
            }
        }
   // }
}

fun saveGrnSequence(currentGrnSequence: String) {
    val userIndicator = currentGrnSequence.substring(0, 1)
    val grnSequence = currentGrnSequence.substring(currentGrnSequence.length - 5).toInt().inc()
    when (grnSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.GRN_SEQUENCE,
            userIndicator.plus("0000".plus(grnSequence.toString()))
        )
        2 -> PreferenceHelper.save(
            Constants.GRN_SEQUENCE,
            userIndicator.plus("000".plus(grnSequence.toString()))
        )
        3 -> PreferenceHelper.save(
            Constants.GRN_SEQUENCE,
            userIndicator.plus("00".plus(grnSequence.toString()))
        )
        4 -> PreferenceHelper.save(
            Constants.GRN_SEQUENCE,
            userIndicator.plus("0".plus(grnSequence.toString()))
        )
        5 -> PreferenceHelper.save(
            Constants.GRN_SEQUENCE,
            userIndicator.plus(grnSequence.toString())
        )
    }
}


fun saveGrnSequence(transactionList: MutableList<VegaReceiving>, isOnline: Boolean) {
    val transbatchNos = transactionList.filter { it.status.equals(Status.SYNC_COMPLETED)||it.status.equals(Status.SYNC_PENDING) }.map { if(it.palletType?.isNotEmpty() == true)it.palletType?.takeLast(5)?.toInt() else 0 }.sortedBy { it }.asReversed()
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentGrnSeq = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
        if (currentGrnSeq.isNotEmpty()) {
            val userIndicator = currentGrnSeq.substring(0, 1)
            var grnSequence = currentGrnSeq.substring(currentGrnSeq.length - 5).toInt().inc()
            if(transbatchNos.isNotEmpty()){
                var maxValue = grnSequence
                if(isOnline)
                    maxValue = transbatchNos.maxByOrNull { it?:0 }?.inc()?:grnSequence
                else
                    maxValue = (transbatchNos.maxByOrNull { it?:0 }?.inc()?:grnSequence)/*.inc()*/
                when{
                    grnSequence != maxValue -> grnSequence = maxValue
                }
            }
            when (grnSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.GRN_SEQUENCE,
                    userIndicator.plus("0000".plus(grnSequence.toString()))
                )
                2 -> PreferenceHelper.save(
                    Constants.GRN_SEQUENCE,
                    userIndicator.plus("000".plus(grnSequence.toString()))
                )
                3 -> PreferenceHelper.save(
                    Constants.GRN_SEQUENCE,
                    userIndicator.plus("00".plus(grnSequence.toString()))
                )
                4 -> PreferenceHelper.save(
                    Constants.GRN_SEQUENCE,
                    userIndicator.plus("0".plus(grnSequence.toString()))
                )
                5 -> PreferenceHelper.save(
                    Constants.GRN_SEQUENCE,
                    userIndicator.plus(grnSequence.toString())
                )
            }
        }
    }
}

fun saveFGRNTallySequence(currentFGRNTallySequence:String){
    val userIndicator = currentFGRNTallySequence.substring(0, 1)
    val tallySequence = currentFGRNTallySequence.substring(currentFGRNTallySequence.length - 5).toInt().inc()
    when (tallySequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.FGRN_TALLY_SEQUENCE, userIndicator.plus("0000").plus(tallySequence.toString())
        )
        2-> PreferenceHelper.save(
            Constants.FGRN_TALLY_SEQUENCE, userIndicator.plus("000").plus(tallySequence.toString())
        )
        3 -> PreferenceHelper.save(
            Constants.FGRN_TALLY_SEQUENCE, userIndicator.plus("00").plus(tallySequence.toString())
        )
        4 -> PreferenceHelper.save(
            Constants.FGRN_TALLY_SEQUENCE, userIndicator.plus("0").plus(tallySequence.toString())
        )
        5 -> PreferenceHelper.save(Constants.FGRN_TALLY_SEQUENCE, userIndicator.plus(tallySequence.toString()))
    }

}

fun saveTallySequence(currentTallySequence: String) {
    val userIndicator = currentTallySequence.substring(0, 1)
    val tallySequence = currentTallySequence.substring(currentTallySequence.length - 5).toInt().inc()
    when (tallySequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.TALLY_SEQUENCE, userIndicator.plus("0000").plus(tallySequence.toString())
        )

        2 -> PreferenceHelper.save(
            Constants.TALLY_SEQUENCE, userIndicator.plus("000").plus(tallySequence.toString())
        )
        3 -> PreferenceHelper.save(
            Constants.TALLY_SEQUENCE,userIndicator.plus("00").plus(tallySequence.toString())
        )
        4 -> PreferenceHelper.save(
            Constants.TALLY_SEQUENCE, userIndicator.plus("0").plus(tallySequence.toString())
        )
        5 -> PreferenceHelper.save(Constants.TALLY_SEQUENCE, userIndicator.plus(tallySequence.toString()))
    }
}

fun saveMtntSequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentMtntSeq = PreferenceHelper.get(Constants.MTNT_SEQUENCE, "")
        if (currentMtntSeq.isNotEmpty()) {
            val userIndicator = currentMtntSeq.substring(0, 1)

            val mtntSequence = currentMtntSeq.substring(currentMtntSeq.length - 5).toInt().inc()
            when (mtntSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.MTNT_SEQUENCE,
                    userIndicator.plus("0000".plus(mtntSequence.toString()))
                )
                2 -> PreferenceHelper.save(
                    Constants.MTNT_SEQUENCE,
                    userIndicator.plus("000".plus(mtntSequence.toString()))
                )
                3 -> PreferenceHelper.save(
                    Constants.MTNT_SEQUENCE,
                    userIndicator.plus("00".plus(mtntSequence.toString()))
                )
                4 -> PreferenceHelper.save(
                    Constants.MTNT_SEQUENCE,
                    userIndicator.plus("0".plus(mtntSequence.toString()))
                )
                5 -> PreferenceHelper.save(
                    Constants.MTNT_SEQUENCE,
                    userIndicator.plus(mtntSequence.toString())
                )
            }
        }
    }
}

fun saveMtnrSequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentMtnrSeq = PreferenceHelper.get(Constants.MTNR_SEQUENCE, "")
        if (currentMtnrSeq.isNotEmpty()) {
            val userIndicator = currentMtnrSeq.substring(0, 1)
            val mtnrSequence = currentMtnrSeq.substring(currentMtnrSeq.length - 5).toInt().inc()
            when (mtnrSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.MTNR_SEQUENCE,
                    userIndicator.plus("0000".plus(mtnrSequence.toString()))
                )
                2 -> PreferenceHelper.save(
                    Constants.MTNR_SEQUENCE,
                    userIndicator.plus("000".plus(mtnrSequence.toString()))
                )
                3 -> PreferenceHelper.save(
                    Constants.MTNR_SEQUENCE,
                    userIndicator.plus("00".plus(mtnrSequence.toString()))
                )
                4 -> PreferenceHelper.save(
                    Constants.MTNR_SEQUENCE,
                    userIndicator.plus("0".plus(mtnrSequence.toString()))
                )
                5 -> PreferenceHelper.save(
                    Constants.MTNR_SEQUENCE,
                    userIndicator.plus(mtnrSequence.toString())
                )
            }
        }
    }
}

fun saveTallySequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentTallySeq = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        if (currentTallySeq.isNotEmpty()) {
            val tallySequence = currentTallySeq.toInt().inc()
            when (tallySequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.TALLY_SEQUENCE, "0000".plus(tallySequence.toString())
                )

                2 -> PreferenceHelper.save(
                    Constants.TALLY_SEQUENCE, "000".plus(tallySequence.toString())
                )
                3 -> PreferenceHelper.save(
                    Constants.TALLY_SEQUENCE, "00".plus(tallySequence.toString())
                )
                4 -> PreferenceHelper.save(
                    Constants.TALLY_SEQUENCE, "0".plus(tallySequence.toString())
                )
                5 -> PreferenceHelper.save(Constants.TALLY_SEQUENCE, tallySequence.toString())

            }
        }
    }
}
fun saveFgrnBatchSequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentBatchSeq = PreferenceHelper.get(Constants.FGRN_BATCH_SEQUENCE, "")
        if (currentBatchSeq.isNotEmpty()) {
            val batchSequence = currentBatchSeq.toInt().inc()
            when (batchSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.FGRN_BATCH_SEQUENCE, "00000".plus(batchSequence.toString())
                )

                2 -> PreferenceHelper.save(
                    Constants.FGRN_BATCH_SEQUENCE, "0000".plus(batchSequence.toString())
                )
                3 -> PreferenceHelper.save(
                    Constants.FGRN_BATCH_SEQUENCE, "000".plus(batchSequence.toString())
                )
                4 -> PreferenceHelper.save(
                    Constants.FGRN_BATCH_SEQUENCE, "00".plus(batchSequence.toString())
                )
                5 -> PreferenceHelper.save(
                    Constants.FGRN_BATCH_SEQUENCE, "0".plus(batchSequence.toString())
                )
                6 -> PreferenceHelper.save(Constants.FGRN_BATCH_SEQUENCE, batchSequence.toString())
            }
        }
    }
}

fun savePileSequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentBatchSeq = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")
        if (currentBatchSeq.isNotEmpty()) {
            val batchSequence = currentBatchSeq.toInt().inc()
            when (batchSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.PILE_SEQUENCE, "000".plus(batchSequence.toString())
                )

                2 -> PreferenceHelper.save(
                    Constants.PILE_SEQUENCE, "00".plus(batchSequence.toString())
                )
                3 -> PreferenceHelper.save(
                    Constants.PILE_SEQUENCE, "0".plus(batchSequence.toString())
                )
                4 -> PreferenceHelper.save(Constants.PILE_SEQUENCE, batchSequence.toString())
            }
        }
    }
}

fun savePOSequence() {
    val isEditTrans = PreferenceHelper.get(Constants.IS_EDIT_TRANS, false)
    if (!isEditTrans) {
        val currentPOSeq = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
        if (currentPOSeq.isNotEmpty()) {
            val userIndicator = currentPOSeq.substring(0, 1)

            val poSequence = currentPOSeq.substring(currentPOSeq.length - 5).toInt().inc()
            when (poSequence.toString().length) {
                1 -> PreferenceHelper.save(
                    Constants.PO_SEQUENCE,
                    userIndicator.plus("0000".plus(poSequence.toString()))
                )
                2 -> PreferenceHelper.save(
                    Constants.PO_SEQUENCE,
                    userIndicator.plus("000".plus(poSequence.toString()))
                )
                3 -> PreferenceHelper.save(
                    Constants.PO_SEQUENCE,
                    userIndicator.plus("00".plus(poSequence.toString()))
                )
                4 -> PreferenceHelper.save(
                    Constants.PO_SEQUENCE,
                    userIndicator.plus("0".plus(poSequence.toString()))
                )
                5 -> PreferenceHelper.save(
                    Constants.PO_SEQUENCE,
                    userIndicator.plus(poSequence.toString())
                )
            }
        }
    }
}

fun getCurrentKeyDetails(): Key {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val splitedKey = currentKey.split("_")
    val key = Key()
    key.receptionType = splitedKey[0]
    key.companyCode = splitedKey[1]
    key.product = splitedKey[2]
    key.backOffice = splitedKey[3]
    return key
}

fun getCurrentKey(): String {
    return PreferenceHelper.get(Constants.CURRENT_KEY, "")
}

fun getCurrentOriginKey(): String {
    val currentOKey = PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
    /*val items = currentOKey.split("-")
    val keyItems = getCurrentKey().split("_")
    if(items.size ==3){
        currentOKey.replace(keyItems.get(1), keyItems.get(1))
    }*/
    return currentOKey
}

fun getCurrentUserName(): String {
    return PreferenceHelper.get(Constants.USER_NAME, "")
}

fun getCurrentOriginEntity(): String {
    return PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
}

fun getCurrentWorkflowDetails(plantOrStorageid: String, currentId: String): WorkflowFields{
    val flowList = Gson().fromJson<List<WorkflowFields>>(PreferenceHelper.get(plantOrStorageid, ""))
    var workFlow = WorkflowFields()
    if (!flowList.isNullOrEmpty())
        workFlow = flowList.find { it.currentId.equals(currentId) } ?: WorkflowFields()
    return workFlow
}

fun getRoles(current_key: String): List<String> {
    return Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
        .filter { key -> key.roleKey.equals(current_key) }.map { it.roleName }
}

fun getFtdcValues(group:String): String {
    val list = Gson().fromJson<List<ftdcItem>>(PreferenceHelper.get(Constants.FTDC_LIST, "")) ?: emptyList()
    val oneItem = list.filter { it.productGroup.equals(group) }
    var ftdc = ""
    ftdc = when(group){
        "ROBU" -> (if(oneItem.isNotEmpty()) oneItem.get(0).ftdc else "").toString()
        "ARAB" -> (if(oneItem.isNotEmpty()) oneItem.get(0).ftdc else "").toString()
        else -> ""
    }
    return  ftdc
}

fun prepareDOQualityWBData(grn: WeighBridge): DOQualityWBDetails {
    val qualityWB = DOQualityWBDetails()
    qualityWB.weighBridgeId = grn.weighBridgeId
    qualityWB.wbTempId = grn.weighBridgeId
    qualityWB.item = grn.item
    qualityWB.plant = grn.plant
    qualityWB.batchNumber = grn.batchNumber
    qualityWB.materialName = grn.materialName
    qualityWB.materialCode = grn.materialCode
    qualityWB.supplierCode = grn.supplierCode
    qualityWB.supplierCode = grn.supplierCode
    qualityWB.deliveryItem = grn.deliveryItem
    qualityWB.netWeight = grn.netWeight.toString()
    qualityWB.isNotWBID = false
    qualityWB.netWeight = grn.netWeight
    qualityWB.qcStatus = grn.qcStatus
    qualityWB.erdat = grn.erdat
    qualityWB.ertim = grn.ertim
    qualityWB.challan = grn.challan
    qualityWB.bagWeight = grn.bagWeight
    qualityWB.bagCount = grn.bagCount
    qualityWB.bagType = grn.bagType
    qualityWB.grossWeight = grn.grossWeight
    qualityWB.unitsOfMeasure = grn.unitsOfMeasure
    return qualityWB
}

fun prepareVegaQualityWBData(grn: WeighBridge): VegaQualityWBDetails {
    val qualityWB = VegaQualityWBDetails()
    qualityWB.weighBridgeId = grn.weighBridgeId
    qualityWB.wbTempId = grn.weighBridgeId
    qualityWB.weighBridgeType = grn.weighBridgeType.toString()
    qualityWB.item = grn.item
    qualityWB.direction = grn.direction.toString()
    qualityWB.plant = grn.plant
    qualityWB.batchNumber = grn.batchNumber
    qualityWB.materialName = grn.materialName
    qualityWB.materialCode = grn.materialCode
    qualityWB.supplierCode = grn.supplierCode
    qualityWB.supplierName = grn.supplierName
    qualityWB.delivery = grn.delivery
    qualityWB.deliveryItem = grn.deliveryItem
    qualityWB.netWeight = grn.netWeight.toString()
    qualityWB.isNotWBID = false
    qualityWB.netWeight = grn.netWeight
    qualityWB.qcStatus = grn.qcStatus
    qualityWB.erdat = grn.erdat
    qualityWB.ertim = grn.ertim
    qualityWB.challan = grn.challan
    qualityWB.bagWeight = grn.bagWeight
    qualityWB.bagCount = grn.bagCount
    qualityWB.bagType = grn.bagType
    qualityWB.grnNumber = grn.grnNumber.toString()
    qualityWB.grossWeight = grn.grossWeight
    qualityWB.unitsOfMeasure = grn.unitsOfMeasure
    qualityWB.unitPrice = grn.unitPrice.toString()
    qualityWB.storageLocation = grn.storageLocationCode.toString()
    qualityWB.storageLocationCode = grn.storageLocationCode.toString()
    qualityWB.purchaseDocDesc = grn.purchaseDocDesc.toString()
    qualityWB.purchaseDocNum = grn.purchaseDocNum.toString()
    return qualityWB
}

fun prepareVegaGrnWBData(grn: WeighBridge): VegaGrnWeighBridgeId {
    val qualityWB = VegaGrnWeighBridgeId()
    qualityWB.weighBridgeId = grn.weighBridgeId
    qualityWB.wbTempId = grn.weighBridgeId
    qualityWB.weighBridgeType = grn.weighBridgeType.toString()
    qualityWB.item = grn.item
    qualityWB.direction = grn.direction.toString()
    qualityWB.batchNumber = grn.batchNumber
    qualityWB.materialName = grn.materialName
    qualityWB.materialCode = grn.materialCode
    qualityWB.supplierCode = grn.supplierCode
    qualityWB.storageLocationCode = grn.storageLocationCode
    qualityWB.supplierName = grn.supplierName
    qualityWB.deliveryItem = grn.deliveryItem
    qualityWB.netWeight = grn.netWeight.toString()
    qualityWB.isNotWBID = false
    qualityWB.netWeight = grn.netWeight.toString()
    qualityWB.qcStatus = grn.qcStatus
    qualityWB.erdat = grn.erdat
    qualityWB.ertim = grn.ertim
    qualityWB.bagWeight = grn.bagWeight
    qualityWB.bagCount = grn.bagCount
    qualityWB.bagType = grn.bagType
    qualityWB.grnNumber = grn.grnNumber.toString()
    qualityWB.grossWeight = grn.grossWeight
    qualityWB.unitsOfMeasure = grn.unitsOfMeasure
    qualityWB.purchaseDocNum = grn.purchaseDocNum
    qualityWB.purchaseDocDesc = grn.purchaseDocDesc
    qualityWB.unitPrice = grn.unitPrice
    return qualityWB
}

/*fun prepareVegaCocoaWbData(wb: WeighBridge): VegaCoCoaQualityWBDetails {
    val qualityWB = VegaCoCoaQualityWBDetails()
    qualityWB.weighBridgeId = wb.weighBridgeId
    qualityWB.wbTempId = wb.weighBridgeId
    qualityWB.weighBridgeType = wb.weighBridgeType.toString()
    qualityWB.item = wb.item
    qualityWB.direction = wb.direction.toString()
    qualityWB.batchNumber = wb.batchNumber
    qualityWB.materialName = wb.materialName
    qualityWB.materialCode = wb.materialCode
    qualityWB.supplierCode = wb.supplierCode
    qualityWB.supplierName = wb.supplierName
    qualityWB.deliveryItem = wb.deliveryItem
    qualityWB.netWeight = wb.netWeight.toString()
    qualityWB.isNotWBID = false
    qualityWB.netWeight = wb.netWeight.toString()
    qualityWB.qcStatus = wb.qcStatus
    qualityWB.erdat = wb.erdat
    qualityWB.ertim = wb.ertim
    qualityWB.bagWeight = wb.bagWeight
    qualityWB.bagCount = wb.bagCount
    qualityWB.bagType = wb.bagType
    qualityWB.grnNumber = wb.grnNumber.toString()
    qualityWB.grossWeight = wb.grossWeight
    qualityWB.unitsOfMeasure = wb.unitsOfMeasure
    qualityWB.purchaseDocNum = wb.purchaseDocNum
    qualityWB.purchaseDocDesc = wb.purchaseDocDesc
    return qualityWB
}*/

fun prepareDOCustomStLocation(customStLocation: List<CustomStLocation>): List<DOCustomStLocation> {
    val locationList = arrayListOf<DOCustomStLocation>()
    customStLocation.forEach {
        val location = DOCustomStLocation()
        location.plant = it.plant
        location.procureLocationCode = it.procureLocationCode.toString()
        location.procureLocationName = it.procureLocationName
        location.storageLocationType = it.storageLocationType
        locationList.add(location)
    }
    return locationList
}

fun prepareDOBinDetails(it: BinDetails): DOBinDetails {
    val location = DOBinDetails()
    location.binLocationCode = it.binLocationCode.toString()
    location.binLocationName = it.binLocationName
    location.procureLocationCode = it.procureLocationCode
    return location
}


// Vega Data

fun prepareVegaPackingMaterial(packageMaterial: List<PackageMaterial>): List<VegaPackageMaterial> {
    val packageMaterialList = arrayListOf<VegaPackageMaterial>()
    packageMaterial.forEach {
        val packageMaterial = VegaPackageMaterial()
        packageMaterial.bagType = it.bagType
        packageMaterial.standardWeight = it.standardWeight
        packageMaterial.tareWeight = it.tareWeight
        packageMaterial.bagMaterialCode = it.bagMaterialCode ?: ""
        packageMaterial.unitsOfMeasure = it.unitsOfMeasure
        packageMaterial.plant = it.plant
        packageMaterial.tolerance = it.tolerance
        packageMaterial.isDefault = it.isDefault
        packageMaterialList.add(packageMaterial)
    }
    return packageMaterialList
}

fun prepareVegaStorageLocation(storageLocation: List<StorageLocation>): List<VegaStorageLocation> {
    val storageLocationList = arrayListOf<VegaStorageLocation>()
    storageLocation.forEach {
        val storageLocation = VegaStorageLocation()
        storageLocation.storageLocationName = it.storageLocationName
        storageLocation.storageLocationCode = it.storageLocationCode
        storageLocation.plant = it.plant
        storageLocationList.add(storageLocation)
    }
    return storageLocationList
}

fun prepareVegaSapMaterial(sapMaterial: List<Material>): List<VegaMaterial> {
    val materialList = arrayListOf<VegaMaterial>()
    sapMaterial.forEach {
        val material = VegaMaterial()
        material.currency = it.currency
        material.materialName = it.materialName
        material.materialCode = it.materialCode
        material.eudrEquivalentMap = it.eudrEquivalentMap
        material.complainceFlag = it.complainceFlag
        material.price = it.price?.toDouble()
        material.plant = it.plant
        material.unitsOfMeasure = it.unitsOfMeasure
        material.materialType = it.materialType
        material.languageCode = it.languageCode
        material.thirdPartyFlag = it.thirdPartyFlag
        material.typeCode = it.typeCode
        material.thirdPartyMaterialCode = it.thirdPartyMaterialCode
        material.productGroup = it.productGroup
        material.cropLimit = it.cropLimit

        materialList.add(material)
    }
    return materialList
}

fun prepareVegaProcessStage(processStage: List<ProcessingStage>): List<VegaProcessingStage> {
    val processStageList = arrayListOf<VegaProcessingStage>()
    processStage.forEach {
        val procesStage = VegaProcessingStage()
        procesStage.auart = it.auart
        procesStage.blendingType = it.blendingType
        procesStage.cfgNo = it.cfgNo ?: ""
        procesStage.fevor = it.fevor
        procesStage.plant = it.plant
        procesStage.process = it.process
        procesStage.processName = it.processName
        processStageList.add(procesStage)
    }
    return processStageList
}

fun prepareCoffeeThirdPartyMaterial(material: List<ThirdPartyMaterialItem>): List<VegaCoffeeThirdPartyMaterialDetail> {
    val materialDetails = arrayListOf<VegaCoffeeThirdPartyMaterialDetail>()
    material.forEach {
        var material = it.materialDetail
        material.mainId = it.id
        material.productType = it.productType
        material.typeCode = it.typeCode
        materialDetails.add(material)
    }
    return materialDetails
}

fun prepareVegaPlanRoute(planRoutes: List<PlanRoute>): List<VegaPlanRoute> {
    val VegaPlanRouteList = arrayListOf<VegaPlanRoute>()
    planRoutes.forEach {
        val planRoute = VegaPlanRoute()
        planRoute.id = it.id!!
        planRoute.departureLocCode = it.departureLocCode
        planRoute.departureLocName = it.departureLocName
        planRoute.routeLocationName = it.routeLocationName
        planRoute.routeLocCode = it.routeLocCode
        planRoute.sourceLocCode = it.sourceLocCode
        VegaPlanRouteList.add(planRoute)
    }
    return VegaPlanRouteList
}

fun prepareVegaMaterialStlocDetails(materialStlocDetails: List<VegaMaterialStlocDetails>): List<VegaStorageLocationDetail> {
    val VegaStoragelocationDetailList = arrayListOf<VegaStorageLocationDetail>()
    materialStlocDetails.forEach {
        val VegaStoragelocationDetail = it.storageLocationDetail
        VegaStoragelocationDetail.forEach { it1 ->
            val storagelocationDetail = VegaStorageLocationDetail()
            storagelocationDetail.materialCode = it.materialCode.toString()
            storagelocationDetail.plant = it1.plant
            storagelocationDetail.storageLocationCode = it1.storageLocationCode
            storagelocationDetail.storageLocationName = it1.storageLocationName
            VegaStoragelocationDetailList.add(storagelocationDetail)
        }
    }
    return VegaStoragelocationDetailList
}

fun prepareVegaVendor(vendors: List<Vendor>): List<VegaVendor> {
    val vendorList = arrayListOf<VegaVendor>()
    vendors.forEach {
        val vendor = VegaVendor()
        vendor.countryCode = it.countryCode
        vendor.vendorAddress = it.vendorAddress
        vendor.vendorCity = it.vendorCity
        vendor.vendorCode = it.vendorCode
        vendor.vendorName = it.vendorName
        vendor.bcApprover = it.bcApprover
        vendor.taxNumber = it.taxNumber
        vendor.vendorType = it.vendorType
        vendor.vendorAdvLimit = it.vendorAdvLimit
        vendor.werks = it.werks?: "123"

        vendor.vendorCustomerCode = it.vendorCustomerCode
        it.purchaseOrgType?.let { type ->
            vendor.purchaseOrgType = type.purchaseType ?: ""
        }
        it.storageLocation?.let { type ->
            vendor.storagelocationcodeName = type.storageLocationCode.plus(" - ").plus(type.storageLocationName?:"")
        }
        vendorList.add(vendor)
    }
    return vendorList
}

fun prepareVegaVehicle(vendors: List<VehicleData>): List<VehicleDetails> {
    val vehicleList = arrayListOf<VehicleDetails>()
    vendors.forEach {
        val vendor = VehicleDetails()
        vendor.vehicleId = (it.vehicleId).toInt()
        vendor.qrCodeNumber = it.qrCodeNumber
        vendor.vehicleNumber = it.vehicleNumber
        vendor.driverLicenseNumber =
            (if (it.driverDetails.isNotEmpty() == true) it.driverDetails.get(0).driverLicenseNumber else "")
        vendor.driverName =
            (if (it.driverDetails.isNotEmpty() == true) it.driverDetails.get(0).driverName else "")
        vendor.driverPhone =
            (if (it.driverDetails.isNotEmpty() == true) it.driverDetails.get(0).driverPhone else "")
        vendor.vendorCode = it.vendor.vendorCode
        vendor.name = it.vendor.name
        vehicleList.add(vendor)
    }
    return vehicleList
}

fun prepareVegaWarehouse(warehouse: List<Warehouse>): List<VegaWarehouse> {
    val warehouseList = arrayListOf<VegaWarehouse>()
    warehouse.forEach {
        val warehouse = VegaWarehouse()
        warehouse.plantId = it.plantId
        warehouse.warehouseId = it.warehouseId
        warehouse.warehouseName = it.warehouseName
        warehouseList.add(warehouse)
    }
    return warehouseList
}

fun prepareVegaQualityParams(qualityParameter: List<QualityParameter>): List<VegaQualityParameter> {
    val qualityParameterList = arrayListOf<VegaQualityParameter>()
    qualityParameter.forEach {
        val qualityParameter = VegaQualityParameter()
        qualityParameter.wbid = it.wbid
        qualityParameter.wbTempId = it.wbTempId
        qualityParameter.materialCode = it.materialCode
        qualityParameter.descrChar = it.descrChar
        qualityParameter.nameChar = it.nameChar
        qualityParameter.entryObligatory = it.entryObligatory
        qualityParameter.unitText = it.unitText
        qualityParameter.dataType = it.dataType
        qualityParameter.unitsOfMeasure = it.unitsOfMeasure
        qualityParameter.numberDigits = it.numberDigits
        qualityParameter.numberDecimals = it.numberDecimals
        qualityParameter.numValFm = it.numValFm
        qualityParameter.numValTo = it.numValTo
        qualityParameter.currValFm = it.currValFm
        qualityParameter.currValTo = it.currValTo
        qualityParameter.valRelatn = it.valRelatn
        qualityParameter.timeStamp = it.timeStamp
        qualityParameter.qualityParameterValue = it.qualityParameterValue
        qualityParameter.isSyncStatus = it.isSyncStatus
        qualityParameter.position = it.position
        qualityParameter.priorityOrder = it.priorityOrder
        qualityParameter.mandatory = it.mandatory
        qualityParameter.singleValue = it.singleValue
        qualityParameter.preSampling = it.preSampling
        qualityParameter.vegaMandatory = it.vegaMandatory
        qualityParameter.vegaValueMandatory = it.vegaValueMandatory
        qualityParameter.qualityParamLabel = it.qualityParamLabel
        qualityParameter.formulaParam = it.formulaParam
        qualityParameterList.add(qualityParameter)
    }

    return qualityParameterList
}
fun prepareVegaQualityParams1(qualityParameter: List<VegaQualityParameter?>): List<VegaQuality> {
    val qualityParameterList = arrayListOf<VegaQuality>()
    qualityParameter.forEach {
        val qualityParameter = VegaQuality()
        qualityParameter.wbid = it?.wbid.toString()
        qualityParameter.wbTempId = it?.wbTempId.toString()
        qualityParameter.materialCode = it?.materialCode.toString()
        qualityParameter.descrChar = it?.descrChar
        qualityParameter.nameChar = it?.nameChar.toString()
        qualityParameter.entryObligatory = it?.entryObligatory
        qualityParameter.unitText = it?.unitText
        qualityParameter.dataType = it?.dataType
        qualityParameter.unitsOfMeasure = it?.unitsOfMeasure
        qualityParameter.numberDigits = it?.numberDigits
        qualityParameter.numberDecimals = it?.numberDecimals
        qualityParameter.numValFm = it?.numValFm
        qualityParameter.numValTo = it?.numValTo
        qualityParameter.currValFm = it?.currValFm
        qualityParameter.currValTo = it?.currValTo
        qualityParameter.valRelatn = it?.valRelatn
        qualityParameter.timeStamp = it?.timeStamp
        qualityParameter.qualityParameterValue = it?.qualityParameterValue
        qualityParameter.isSyncStatus = it?.isSyncStatus!!
        qualityParameter.position = it.position
        /* qualityParameter?.priorityOrder = it?.priorityOrder
         qualityParameter?.mandatory = it?.mandatory
         qualityParameter?.singleValue = it?.singleValue*/
        qualityParameter.preSampling = it.preSampling
        qualityParameter.vegaMandatory = it.vegaMandatory
        // qualityParameter?.vegaValueMandatory = it?.vegaValueMandatory
        qualityParameter.qualityParamLabel = it.qualityParamLabel
        qualityParameter.formulaParam = it.formulaParam
        qualityParameterList.add(qualityParameter)
    }

    return qualityParameterList
}

fun prepareVegaQualitative(data: Qualitative): VegaQualitative {
    val qualitative = VegaQualitative()
    if(data.nameChar=="LOBM_UDCODE"){
    qualitative.charValue = data.charValue
    }else {
        qualitative.charValue = data.charValue.replace("\\s+".toRegex(), " ")
    }
    qualitative.descValue = data.descValue
    qualitative.materialCode = data.materialCode
    qualitative.nameChar = data.nameChar

    return qualitative
}

fun prepareVegaCustomStLocation(customStLocation: List<CustomStLocation>): List<VegaCustomStLocation> {
    val locationList = arrayListOf<VegaCustomStLocation>()
    customStLocation.forEach {
        val location = VegaCustomStLocation()
        location.plant = it.plant.toString()
        location.procureLocationCode = it.procureLocationCode.toString()
        location.procureLocationName = it.procureLocationName
        location.storageLocationType = it.storageLocationType
        locationList.add(location)
    }
    return locationList
}

fun prepareVegaBinDetails(it: BinDetails): VegaBinDetails {
    val location = VegaBinDetails()
    location.binLocationCode = it.binLocationCode.toString()
    location.binLocationName = it.binLocationName
    location.procureLocationCode = it.procureLocationCode
    return location
}

fun prepareVegaBcZoneMapping(bc: List<VegaBcZoneMapping>): List<VegaBcZoneMapping> {
    val bcList = arrayListOf<VegaBcZoneMapping>()
    bc.forEach {
        val bcMapping = VegaBcZoneMapping()
        bcMapping.sapuserId = it.sapuserId
        bcMapping.bczone = it.bczone
        bcList.add(bcMapping)
    }
    return bcList
}

fun getBase64FromFile(path: String?): String? {
    var bmp: Bitmap? = null
    var baos: ByteArrayOutputStream? = null
    var baat: ByteArray? = null
    var encodeString: String? = null
    try {
        bmp = BitmapFactory.decodeFile(path)
        baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        baat = baos.toByteArray()
        encodeString = Base64.encodeToString(baat, Base64.DEFAULT)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return encodeString
}

fun getLineItemFromReceivingLineItem(postData: MutableList<VegaReceivingLineItem>): ArrayList<VegaReceiving> {
    val items = ArrayList<VegaReceiving>()
    postData.forEach {
        val item = VegaReceiving()
        item.tmpWbId = it.tmpWbId
        item.weighBridgeId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.bagTareWeight = it.bagTareWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.charg = it.charg
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight.toString()
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        items.add(item)
    }
    return items
}

fun getMtntFromMtntLineItem(postData: MutableList<VegaMtntLineItem>): ArrayList<VegaMtnt> {
    val items = ArrayList<VegaMtnt>()
    postData.forEach {
        val item = VegaMtnt()
        item.tmpWbId = it.tmpWbId
        item.bagCount = it.bagCount
        item.bagType = it.bagType
        item.bagWeight = it.bagWeight
        item.palletCount = it.palletCount
        item.palletType = it.palletType
        item.palletWeight = it.palletWeight
        item.batchNumber = it.batchNumber
        item.grossWeight = it.grossWeight
        item.item = it.item
        item.materialCode = it.materialCode
        item.materialName = it.materialName
        item.netWeight = it.netWeight.toString()
        item.supplierCode = it.supplierCode
        item.posnr = it.posnr
        item.unitsOfMeasure = it.unitsOfMeasure
        item.plantId = it.plantId
        item.wsGate = it.wsGate
        item.weighBridgeType = it.weighBridgeType
        item.location = it.location
        item.tareWeight = it.tareWeight
        item.supplierName = it.supplierName
        item.isSynced = it.isSynced
        item.status = it.status
        item.syncStatusMsg = it.syncStatusMsg
        item.mtnCode = it.mtnCode
        item.doWeightThreshold = it.doWeightThreshold
        item.truckDirection = it.truckDirection
        item.imagePath = it.imagePath
        item.imageString = it.imageString
        item.vehicleNumber = it.vehicleNumber
        item.vehicleType = it.vehicleType
        item.contactNumber = it.contactNumber
        item.driverName = it.driverName
        item.erdat = it.erdat
        item.ertim = it.ertim
        item.direction = it.direction
        item.transportVendorCode = it.transportVendorCode
        item.transportVendorName = it.transportVendorName
        item.storageLocationCode = it.storageLocationCode
        item.recStorageLocationCode = it.recStorageLocationCode
        item.recPlantId = it.recPlantId
        item.bagTareWeight = it.bagTareWeight
        items.add(item)
    }
    return items
}

fun preparePurcheseOrder(it1: MutableList<PurchaseOrder>): ArrayList<VegaEcuadorPurchaseOrder> {
    val poList = ArrayList<VegaEcuadorPurchaseOrder>()
    it1.forEach { item ->
        val po = VegaEcuadorPurchaseOrder()
        po.poId = item.poId
        po.warehouseId = item.warehouseId
        po.ebelp = item.ebelp
        po.poType = item.poType
        po.material = item.material
        po.supplier = item.supplier
        po.menge = item.menge
        po.meins = item.meins
        po.charg = item.charg
        po.materialNumber = item.materialNumber
        po.materialDesc = item.materialDesc ?: ""
        po.supplierName = item.supplierName
        po.supplierCode = item.supplierCode
        po.bsart = item.bsart
        po.ekorg = item.ekorg
        po.ekgrp = item.ekgrp
        po.storageLocationCode = item.storageLocationCode
        po.storageLocationName = item.storageLocationName ?: ""
        po.recPlantId = item.recPlantId
        po.openQuantity=item.openQuantity
        po.unitPrice=item.unitPrice
        poList.add(po)
    }
    return poList
}


fun prepareVirtualPurchaseOrder(it1: MutableList<PurchaseOrder>): ArrayList<VegaCocoaPurchaseOrders> {
    val poList = ArrayList<VegaCocoaPurchaseOrders>()
    it1.forEach { item ->
        val po = VegaCocoaPurchaseOrders()
        po.purchaseDocNum = item.poId
        po.purchaseOrderType = item.poType
        po.materialCode = item.material
        po.purchaseDocDesc = item.ebelp
        po.supplierCode = item.supplierCode
        po.supplierName = item.supplierName
        po.plantId = item.recPlantId
        po.storageLocationCode = item.storageLocationCode
        po.storageLocationName = item.storageLocationName
        po.issueLocation = item.issueLocation
        po.materialName = item.materialDesc
        po.menge = item.menge
        po.meins = item.meins
        po.openQuantity = item.openQuantity
        po.createdDate = item.createdDate
        po.warehouseId = item.warehouseId
        poList.add(po)
    }
    return poList
}

fun prepareIndoPurchaseOrder(it1: MutableList<PurchaseOrder>): ArrayList<VegaEcuadorDispatchPurchaseOrders> {
    val poList = ArrayList<VegaEcuadorDispatchPurchaseOrders>()
    it1.forEach { item ->
        val po = VegaEcuadorDispatchPurchaseOrders()
        po.purchaseDocNum = item.poId
        po.purchaseOrderType = item.poType
        po.materialCode = item.material
        po.purchaseDocDesc = item.ebelp
        po.supplierCode = item.supplierCode
        po.supplierName = item.supplierName
        po.plantId = item.recPlantId
        po.storageLocationCode = item.storageLocationCode
        po.storageLocationName = item.storageLocationName
        po.materialName = item.materialDesc
        po.menge = item.menge
        po.meins = item.meins
        po.openQuantity = item.openQuantity
        po.warehouseId = item.warehouseId
        poList.add(po)
    }
    return poList
}


fun prepareMtntPurchaseOrder(it1: MutableList<MtntPurchaseOrder>): ArrayList<VegaEcuadorDispatchPurchaseOrders> {
    val poList = ArrayList<VegaEcuadorDispatchPurchaseOrders>()
    it1.forEach { item ->
        val po = VegaEcuadorDispatchPurchaseOrders()
        po.purchaseDocNum = item.poId
        po.purchaseOrderType = item.poType
        po.materialCode = item.material
        po.purchaseDocDesc = item.ebelp
        po.batchNumber = item.charg
        po.plantId = item.recPlantId
        po.openQuantity = item.openQuantity
        po.menge = item.menge
        po.meins = item.meins
        po.bsart = item.bsart
        po.materialName = item.materialName
        po.supplierName = item.supplierName
        po.supplierCode = item.supplierCode
        po.storageLocationCode = item.storageLocationCode
        po.storageLocationName = item.storageLocationName ?: ""
        po.warehouseId = item.warehouseId
        po.ekgrp = item.ekgrp
        po.ekorg = item.ekorg
        poList.add(po)
    }
    return poList
}

fun prepareMtnBatchNumberList(it1: MutableList<VegaGhanaMtnBatchNumber>): ArrayList<VegaGhanaQualityMtnBatch> {
    val mtnBatchList = ArrayList<VegaGhanaQualityMtnBatch>()
    it1.forEach { item ->
        val mtnBatch = VegaGhanaQualityMtnBatch()
        mtnBatch.mtnNumber = item.mtnNumber
        mtnBatch.posnr = item.posnr
        mtnBatch.batchNumber = item.mtnBatchDetails.batchNumber
        mtnBatch.deliveryQty = item.mtnBatchDetails.deliveryQty
        mtnBatch.deliveryUOM = item.mtnBatchDetails.deliveryUOM
        mtnBatch.stockQty = item.mtnBatchDetails.stockQty
        mtnBatch.stockUOM = item.mtnBatchDetails.stockUOM
        mtnBatch.storageLocationCode = item.mtnBatchDetails.storageLocationCode
        mtnBatch.werks = item.mtnBatchDetails.werks
        mtnBatchList.add(mtnBatch)
    }
    return mtnBatchList
}

fun prepareLotQualityDetailsList(it1: MutableList<VegaGhanaLotQuality>): ArrayList<VegaGhanaLotQualityDetails> {
    val lotQualityList = ArrayList<VegaGhanaLotQualityDetails>()
    it1.forEach { item ->
        item.qualityParameters.forEach {
            val lotQuality = VegaGhanaLotQualityDetails()
            lotQuality.materialNumber = item.materialNumber
            lotQuality.charg = item.charg
            lotQuality.sapQCName = it.sapQCName
            lotQuality.satNam = it.satNam
            lotQualityList.add(lotQuality)
        }
    }
    return lotQualityList
}

fun prepareRminBomList(it1: MutableList<VegaGhanaRminBom>): List<VegaProcessingRminBoms> {
    var bomList = ArrayList<VegaProcessingRminBoms>()
    it1.forEach { item ->
        item.boms.forEach {
            val lotQuality = VegaProcessingRminBoms()
            lotQuality.materialCode = it.materialCode!!
            lotQuality.materialName = it.materialName!!
            lotQuality.versionId = it.versionId
            lotQuality.inputMaterialCode = it.inputMaterialCode
            lotQuality.cfgno = it.cfgno!!
            lotQuality.baseMaterialCode = it.materialCode!!
            bomList.add(lotQuality)
        }

    }
    return bomList
}


fun prepareStocksList(it1: MutableList<Stocks>): ArrayList<VegaEcuadorDispatchStocks> {
    val stockList = ArrayList<VegaEcuadorDispatchStocks>()
    it1.forEach { item ->
        val stock = VegaEcuadorDispatchStocks()
        stock.batchNumber = item.batchNumber
        stock.bkBez = item.bkBez
        stock.bkLas = item.bkLas
        stock.cinsm = item.cinsm
        stock.materialCode = item.materialCode.toString()
        stock.materialName = item.materialName
        stock.materialText = item.materialText
        stock.plantId = item.plantId
        stock.plantName = item.plantName
        stock.storageLocationCode = item.storageLocationCode.toString()
        stock.unitOfMeasure = item.unitOfMeasure
        stock.weight = item.weight
        stockList.add(stock)
    }
    return stockList
}

fun preparePriceConfigDetails(priceConfigDetailsList: List<PriceConfigDetails>): ArrayList<VegaNicaraguaPriceConfigDetails> {

    val vegaPriceConfigDetails = ArrayList<VegaNicaraguaPriceConfigDetails>()
    priceConfigDetailsList.forEach {
        val priceConfigDetails = VegaNicaraguaPriceConfigDetails()
        priceConfigDetails.id = it.id
        priceConfigDetails.qualityCode = it.qualityCode.toString()
        priceConfigDetails.certificatePremium = it.certificatePremium
        priceConfigDetails.volumePremiumFrom = it.volumePremiumFrom
        priceConfigDetails.volumePremiumTo = it.volumePremiumTo
        priceConfigDetails.volumePremium = it.volumePremium
        priceConfigDetails.moisturePremium = it.moisturePremium
        priceConfigDetails.qualityDiscount = it.qualityDiscount
        priceConfigDetails.moistureDiscount = it.moistureDiscount
        priceConfigDetails.dailyPrice = it.dailyPrice
        priceConfigDetails.exportIncentive = it.exportIncentive
        priceConfigDetails.ftdc = it.ftdc
        it.plant?.let { plant ->
            priceConfigDetails.plantName = plant.plantName
            priceConfigDetails.plantId = plant.plantId
        }
        it.materialDetail?.let { material ->
            priceConfigDetails.materialName = material.materialName
            priceConfigDetails.materialCode =
                if (material.materialCode.length != 18) "000000".plus(material.materialCode) else material.materialCode
        }
        it.pricingCommission?.let { pricingCommission ->
            priceConfigDetails.withHoldingTax = pricingCommission.withHoldingTax
            priceConfigDetails.neCommission = pricingCommission.neCommission
            priceConfigDetails.bankCommsion = pricingCommission.bankCommsion
            // priceConfigDetails.ftdc = pricingCommission.ftdc
        }
        it.grade?.let { grade ->
            priceConfigDetails.grade = grade.grade
            if (it.qualityCode.isNullOrEmpty()) priceConfigDetails.qualityCode = grade.gradeCode.toString()
        }
        vegaPriceConfigDetails.add(priceConfigDetails)
    }
    return vegaPriceConfigDetails
}

fun preparePositionGradeMappings(positionGradeMappings: List<PositionGradeMappings>): ArrayList<VegaNicaraguaPositionGradeMappings> {
    val vegaPositionGradeMappings = ArrayList<VegaNicaraguaPositionGradeMappings>()
    positionGradeMappings.forEach {
        val positionGradeMapping = VegaNicaraguaPositionGradeMappings()
        positionGradeMapping.id = it.id
        positionGradeMapping.companyCode = it.companyCode
        positionGradeMapping.codeGroup = it.codeGroup
        positionGradeMapping.code = it.code
        positionGradeMapping.description = it.description
        positionGradeMapping.createdAt = it.createdAt
        positionGradeMapping.createdBy = it.createdBy
        positionGradeMapping.updatedAt = it.updatedAt
        positionGradeMapping.updatedBy = it.updatedBy

        vegaPositionGradeMappings.add(positionGradeMapping)
    }
    return vegaPositionGradeMappings
}

fun prepareGrnPriceDetailsList(grnPriceDetails: List<GrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {
    val grnPriceDetailsList = ArrayList<VegaNicaraguaGrnPriceDetails>()
    grnPriceDetails.forEach {

        val grnPriceDetail = VegaNicaraguaGrnPriceDetails()
        grnPriceDetail.companyCode = it.companyCode
        grnPriceDetail.division = it.division
        grnPriceDetail.plant = it.plant
        grnPriceDetail.purchasingOrg = it.purchasingOrg
        grnPriceDetail.purchasingGroup = it.purchasingGroup
        grnPriceDetail.fieldName = it.fieldName!!
        grnPriceDetail.description = it.description
        grnPriceDetail.priceDate = it.priceDate
        grnPriceDetail.price = it.price
        grnPriceDetail.differential = it.differential
        grnPriceDetail.currency = it.currency
        grnPriceDetail.baseUnit = it.baseUnit
        grnPriceDetail.createdOn = it.createdOn

        grnPriceDetailsList.add(grnPriceDetail)
    }

    return grnPriceDetailsList
}

fun prepareExchangeRate(exchangeRate: ExchangeRate): VegaNicaraguaExchangeRate {
    val vegaNicaraguaExchangeRate = VegaNicaraguaExchangeRate()
    vegaNicaraguaExchangeRate.key = getCurrentKey()
    vegaNicaraguaExchangeRate.currencyCode = exchangeRate.currencyCode!!
    vegaNicaraguaExchangeRate.currencyValue = exchangeRate.currencyValue
    vegaNicaraguaExchangeRate.exchangeRate = exchangeRate.exchangeRate

    return vegaNicaraguaExchangeRate
}

fun prepareAdvanceLineDetails(advanceLineDetails: List<AdvanceLineItems>): ArrayList<VegaNicaraguaAdvanceLineItems> {
    val advanceLineDetailsList = ArrayList<VegaNicaraguaAdvanceLineItems>()
    advanceLineDetails.forEach { response ->

        val advanceLineItemsDetails = response.advanceLineItemDetails

        advanceLineItemsDetails!!.forEach {
            val advanceLineItem = VegaNicaraguaAdvanceLineItems()
            advanceLineItem.documentNumber = it.documentNumber!!
            advanceLineItem.financialYear = it.financialYear
            advanceLineItem.currency = it.currency
            advanceLineItem.postingDate = it.postingDate
            advanceLineItem.documentDate = it.documentDate
            advanceLineItem.baselineDate = it.baselineDate
            advanceLineItem.indicator = it.indicator
            advanceLineItem.businessArea = it.businessArea
            advanceLineItem.amount = it.amount
            advanceLineItem.itemNum = it.itemNum
            advanceLineItem.deletedFlag = it.deletedFlag
            advanceLineItem.companyCode = it.companyCode
            advanceLineItem.vendor = it.vendor!!

            advanceLineDetailsList.add(advanceLineItem)
        }

    }
    return advanceLineDetailsList
}

fun prepareAdvanceLineDetailsReverse(advanceLineItems: List<VegaNicaraguaAdvanceLineItemGrn>): ArrayList<AdvanceLineItemDetails> {
    val advanceLineDetailsList = ArrayList<AdvanceLineItemDetails>()
    advanceLineItems.forEach {
        val advanceLineItem = AdvanceLineItemDetails()
        advanceLineItem.documentNumber = it.documentNumber
        advanceLineItem.advanceKnockAmount = it.advanceKnockAmount
        advanceLineItem.totalAdvanceKnockAmount = it.totalAdvanceKnockAmount
        advanceLineItem.interestAmount = it.interestAmount
        advanceLineItem.commissionAmount = it.commissionAmount
        advanceLineItem.legalExpenseAmount = it.legalExpenseAmount
        advanceLineItem.currencyDevaluationAmount = it.currencyDevaluationAmount

        advanceLineDetailsList.add(advanceLineItem)
    }
    return advanceLineDetailsList
}

fun prepareGrnCharDetails(grnCharDetails: List<GrnCharDetails>): ArrayList<VegaNicaraguaGrnCharDetails> {
    val grnCharDetailsList = ArrayList<VegaNicaraguaGrnCharDetails>()
    grnCharDetails.forEach {
        it.characterstics.forEach { char ->
            val grnCharDetail = VegaNicaraguaGrnCharDetails()
            grnCharDetail.materialNumber = char?.materialNumber.toString()
            grnCharDetail.materialName = char?.materialName
            grnCharDetail.plant = char?.plant
            grnCharDetail.qualityParamName = char?.qualityParamName.toString()
            grnCharDetail.grade = char?.grade.toString()
            grnCharDetail.qualityParamDesc = char?.qualityParamDesc
            grnCharDetail.erdate = char?.erdate
            grnCharDetail.effectiveDate = char?.effectiveDate
            grnCharDetail.charValue = char?.charValue
            grnCharDetail.numValue = char?.numValue
            grnCharDetail.toValue = char?.toValue

            grnCharDetailsList.add(grnCharDetail)
        }
    }
    return grnCharDetailsList
}

fun prepareStocksToInventoryDeatils(
    dataValue: List<VegaCocoaDispatchLots>,
    productList: List<VegaMaterial>
): ArrayList<VegaNicaraguaGRNInventoryDetails> {
    val inventoryStocks = ArrayList<VegaNicaraguaGRNInventoryDetails>()
    dataValue.forEach {
        val inven = VegaNicaraguaGRNInventoryDetails()
        inven.plantId = it.plantId
        inven.plantName = it.plantName
        inven.procureLocationCode = it.storageLocationCode
        inven.materialCode = it.materialCode
        val productName = productList.filter { inven.materialCode.contains(it.materialCode) }
        if (productName.size > 0) inven.materialName = productName[0].materialName
        inven.lotId = it.batchNumber
        inven.stockQty = it.weight
        inven.qualityGrade = ""
        inven.certification = ""
        inven.vendorCode = it.vendor
        inven.vendorName = it.vendorName
        inven.uom = it.unitOfMeasure
        inven.bagCount = it.noOfBags
        inventoryStocks.add(inven)
    }
    return inventoryStocks
}

fun prepareGrnInventoryDetails(
    inventoryResPonse: OfflineInventory,
    productList: List<VegaMaterial>
): ArrayList<VegaNicaraguaGRNInventoryDetails> {

    val vegaInventorygDetails = ArrayList<VegaNicaraguaGRNInventoryDetails>()

    val inventoryRes = inventoryResPonse.inventory
    inventoryRes.forEach { inventory ->
        var bagsQuantity: String? = "0"
        var tareWeight: String? = "0"
        var humidity: String? = "0"
        var grossWeight: String? = "0"
        var qualityGrade: String? = ""
        var certification: String? = ""
        var exportable: String? = "0"

        val inventoryDetails = VegaNicaraguaGRNInventoryDetails()
        inventoryDetails.id = inventory.id
        inventory.warehouseLocation?.let { wareHouseLocation ->
            inventoryDetails.warehouseId = wareHouseLocation.warehouse?.warehouseId
            inventoryDetails.warehouseName = wareHouseLocation.warehouse?.warehouseName
            inventoryDetails.plantId = wareHouseLocation.warehouse?.plant?.plantCode
            inventoryDetails.plantName = wareHouseLocation.warehouse?.plant?.plantName
            inventoryDetails.procureLocationCode = wareHouseLocation.procureLocationCode
            inventoryDetails.procureLocationName = wareHouseLocation.procureLocationName
        }
        inventoryDetails.materialCode = inventory.materialCode ?: ""
        val productName =
            productList.filter { inventory.materialCode?.contains(it.materialCode) == true }
        if (productName.size > 0) inventoryDetails.materialName = productName[0].materialName
        inventoryDetails.lotId = inventory.lotId ?: ""
        inventoryDetails.vendorCode = inventory.vendorCode
        inventoryDetails.uom = inventory.uom
        inventoryDetails.stockQty = inventory.stockQty
        inventoryDetails.grnDate = inventory.grnDate
        inventoryDetails.createdDateTime = inventory.createdDateTime
        inventoryDetails.createdBy = inventory.createdBy
        inventoryDetails.updatedAt = inventory.updatedAt
        inventoryDetails.updatedBy = inventory.updatedBy
        inventoryDetails.qualityGrade = inventory.gradeDesc
        inventoryDetails.certification = inventory.certification
        inventory.inventoryQC?.forEach {
            inventoryDetails.qcId = it.qcId
            inventoryDetails.qcName = it.qcName ?: ""
            inventoryDetails.dataType = it.dataType
            inventoryDetails.numberDecimals = it.numberDecimals
            inventoryDetails.entryObligatory = it.entryObligatory
            inventoryDetails.value = it.value
            if (it.qcName.equals("NISACOS")) {
                bagsQuantity = it.value
            } else if (it.qcName.equals("NIPESBRT")) {
                grossWeight = it.value
            } else if (it.qcName.equals("NIPESTAR")) {
                tareWeight = it.value
            } else if (it.qcName.equals("NIRM0003")) {
                humidity = it.value
            } else if (it.qcName.equals("NIRM0010")) {
                exportable = it.value?.replace("%", "")?.trim()
            }
        }

        inventoryDetails.bagCount =
            if (bagsQuantity?.isNotEmpty() == true) bagsQuantity?.replace(",", "")?.toDouble()
                ?.formatThreeDigits() else "0"
        inventoryDetails.tareWeight =
            if (tareWeight?.isNotEmpty() == true) tareWeight?.split(" ")?.get(0)?.replace(",", "")
                ?.toDouble()
                ?.formatThreeDigits() else "0"
        inventoryDetails.grossWeight =
            if (grossWeight?.isNotEmpty() == true) grossWeight?.split(" ")?.get(0)?.replace(",", "")
                ?.toDouble()
                ?.formatThreeDigits() else "0"
        inventoryDetails.humidity = if (humidity?.isNotEmpty() == true) humidity else "0"
        inventoryDetails.plantId = getPlantDetails().plantId
        inventoryDetails.plantName = getPlantDetails().plantName
        inventoryDetails.exportable = if (exportable?.isNotEmpty() == true) exportable else "0"

        vegaInventorygDetails.add(inventoryDetails)
    }
    return vegaInventorygDetails
}

fun prepareVegaMaterialQualityGrades(materialQualityGrades: List<MaterialQualitGrades>): List<VegaNicaraguaMaterialQualitGrades> {
    val vegaQualityGradeList = arrayListOf<VegaNicaraguaMaterialQualitGrades>()
    materialQualityGrades.forEach {

        val materialDetails = it.materialDetails

        it.qualityGrades?.forEach {
            val vegaQualityGrade = VegaNicaraguaMaterialQualitGrades()

            vegaQualityGrade.materialCode = "000000".plus(materialDetails?.materialCode!!)
            vegaQualityGrade.materialName = materialDetails.materialName
            vegaQualityGrade.materialType = materialDetails.materialType
            vegaQualityGrade.bagType = it.bagType!!
            vegaQualityGrade.tareWeight = it.bagTareWeight
            vegaQualityGrade.price = materialDetails.price
            vegaQualityGrade.unitsOfMeasure = materialDetails.unitsOfMeasure
            vegaQualityGrade.currency = materialDetails.currency
            vegaQualityGrade.grade = it.grade
            vegaQualityGrade.gradeCode = it.gradeCode!!

            vegaQualityGradeList.add(vegaQualityGrade)
        }

    }
    return vegaQualityGradeList
}

fun checkBatchSequence(batchNumber: String): Boolean {
    try {
        val lotSequence = batchNumber.substring(batchNumber.length - 5).toInt()
        val currentBatchNumber = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
        if (currentBatchNumber.equals("")) return true
        val currentLotSequence = currentBatchNumber.substring(currentBatchNumber.length - 5).toInt()
        return lotSequence > currentLotSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkInvoiceSequence(batchNumber: String): Boolean {
    try {
        val lotSequence = batchNumber.substring(batchNumber.length - 5).toInt()
        val currentBatchNumber = PreferenceHelper.get(Constants.INVOICE_SEQUENCE, "")
        if (currentBatchNumber.equals("")) return true
        val currentLotSequence = currentBatchNumber.substring(currentBatchNumber.length - 5).toInt()
        return lotSequence > currentLotSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkGrnSequence(grnSequnce: String): Boolean {
    try {
        val grnSequence = grnSequnce.substring(grnSequnce.length - 5).toInt()
        val currentGrnSequnce = PreferenceHelper.get(Constants.GRN_SEQUENCE, "")
        if (currentGrnSequnce.equals("")) return true
        val currentGrnSequence = currentGrnSequnce.substring(currentGrnSequnce.length - 5).toInt()
        return grnSequence > currentGrnSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkMtntSequence(mtntSequnce: String): Boolean {
    try {
        val mtntSequnce = mtntSequnce.substring(mtntSequnce.length - 5).toInt()
        val currentMtntSequnce = PreferenceHelper.get(Constants.MTNT_SEQUENCE, "")
        if (currentMtntSequnce.equals("")) return true
        val currentMtntSequence =
            currentMtntSequnce.substring(currentMtntSequnce.length - 5).toInt()
        return mtntSequnce > currentMtntSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkMtnrSequence(mtnrSequnce: String): Boolean {
    try {
        val mtnrSequnce = mtnrSequnce.substring(mtnrSequnce.length - 5).toInt()
        val currentMtnrSequnce = PreferenceHelper.get(Constants.MTNR_SEQUENCE, "")
        if (currentMtnrSequnce.equals("")) return true
        val currentMtnrSequence =
            currentMtnrSequnce.substring(currentMtnrSequnce.length - 5).toInt()
        return mtnrSequnce > currentMtnrSequence
    } catch (e: Exception) {
        return false
    }
}


fun checkFGRNTallySequence(fgrnTallySequence:String):Boolean{
    try {
        val tallySequence = fgrnTallySequence.substring(fgrnTallySequence.length - 5).toInt()
        val currentTallySequnce = PreferenceHelper.get(Constants.FGRN_TALLY_SEQUENCE, "")
        if (currentTallySequnce.equals("")) return true
        val currentTallySequence = currentTallySequnce.substring(fgrnTallySequence.length - 5).toInt()
        return tallySequence > currentTallySequence
    } catch (e: Exception) {
        return false
    }
}

fun checkTallySequence(tallySequnce: String): Boolean {
    try {
        val tallySequence =  tallySequnce.substring(tallySequnce.length - 5).toInt()
        val currentTallySequnce = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        if (currentTallySequnce.equals("")) return true
        val currentTallySequence = currentTallySequnce.substring(currentTallySequnce.length - 5).toInt()
        return tallySequence > currentTallySequence
    } catch (e: Exception) {
        return false
    }
}

fun checkFgrnBatchSequence(batchSequnce: String): Boolean {
    try {
        val batchSequence = batchSequnce.substring(batchSequnce.length - 5).toInt()
        val currentBatchSequnce = PreferenceHelper.get(Constants.FGRN_BATCH_SEQUENCE, "")
        if (currentBatchSequnce.equals("")) return true
        val currentBatchSequence = currentBatchSequnce.substring(batchSequnce.length - 5).toInt()
        return batchSequence > currentBatchSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkPileSequence(batchSequnce: String): Boolean {
    try {
        val batchSequence = batchSequnce.substring(batchSequnce.length - 4).toInt()
        val currentBatchSequnce = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")
        if (currentBatchSequnce.equals("")) return true
        val currentBatchSequence = currentBatchSequnce.substring(currentBatchSequnce.length - 4).toInt()
        return batchSequence > currentBatchSequence
    } catch (e: Exception) {
        return false
    }
}

fun checkPoSequence(poSequnce: String): Boolean {
    try {
        val poSequence = poSequnce.substring(poSequnce.length - 5).toInt()
        val currentPOSequnce = PreferenceHelper.get(Constants.PO_SEQUENCE, "")
        if (currentPOSequnce.equals("")) return true
        val currentPoSequence = currentPOSequnce.substring(currentPOSequnce.length - 5).toInt()
        return poSequence > currentPoSequence
    } catch (e: Exception) {
        return false
    }
}


fun saveLotSequence(currentLotSequence: String) {
    try{
        val userIndicator = currentLotSequence.substring(0, 1)
        val lotSequence = currentLotSequence.substring(currentLotSequence.length - 5).toInt().inc()
        when (lotSequence.toString().length) {
            1 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("0000".plus(lotSequence.toString())))
            2 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("000".plus(lotSequence.toString())))
            3 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("00".plus(lotSequence.toString())))
            4 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus("0".plus(lotSequence.toString())))
            5 -> PreferenceHelper.save(Constants.LOT_SEQUENCE, userIndicator.plus(lotSequence.toString()))
        }
    }catch (e:Exception){e.printStackTrace()}
}

fun saveInvoiceSequenceMaster(currentInvoiceSequence: String) {
    val userIndicator = currentInvoiceSequence.substring(0, 1)
    val invoiceSequence = currentInvoiceSequence.substring(currentInvoiceSequence.length - 5).toInt().inc()
    when (invoiceSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.INVOICE_SEQUENCE,
            userIndicator.plus("0000".plus(invoiceSequence.toString()))
        )
        2 -> PreferenceHelper.save(
            Constants.INVOICE_SEQUENCE,
            userIndicator.plus("000".plus(invoiceSequence.toString()))
        )
        3 -> PreferenceHelper.save(
            Constants.INVOICE_SEQUENCE,
            userIndicator.plus("00".plus(invoiceSequence.toString()))
        )
        4 -> PreferenceHelper.save(
            Constants.INVOICE_SEQUENCE,
            userIndicator.plus("0".plus(invoiceSequence.toString()))
        )
        5 -> PreferenceHelper.save(
            Constants.INVOICE_SEQUENCE,
            userIndicator.plus(invoiceSequence.toString())
        )
    }
}


fun saveMtntSequence(currentMtntSequence: String) {
    val userIndicator = currentMtntSequence.substring(0, 1)
    val mtntSequence = currentMtntSequence.substring(currentMtntSequence.length - 5).toInt().inc()
    when (mtntSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.MTNT_SEQUENCE,
            userIndicator.plus("0000".plus(mtntSequence.toString()))
        )
        2 -> PreferenceHelper.save(
            Constants.MTNT_SEQUENCE,
            userIndicator.plus("000".plus(mtntSequence.toString()))
        )
        3 -> PreferenceHelper.save(
            Constants.MTNT_SEQUENCE,
            userIndicator.plus("00".plus(mtntSequence.toString()))
        )
        4 -> PreferenceHelper.save(
            Constants.MTNT_SEQUENCE,
            userIndicator.plus("0".plus(mtntSequence.toString()))
        )
        5 -> PreferenceHelper.save(
            Constants.MTNT_SEQUENCE,
            userIndicator.plus(mtntSequence.toString())
        )
    }
}

fun saveMtnrSequence(currentMtnrSequence: String) {
    val userIndicator = currentMtnrSequence.substring(0, 1)
    val mtnrSequence = currentMtnrSequence.substring(currentMtnrSequence.length - 5).toInt().inc()
    when (mtnrSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.MTNR_SEQUENCE,
            userIndicator.plus("0000".plus(mtnrSequence.toString()))
        )
        2 -> PreferenceHelper.save(
            Constants.MTNR_SEQUENCE,
            userIndicator.plus("000".plus(mtnrSequence.toString()))
        )
        3 -> PreferenceHelper.save(
            Constants.MTNR_SEQUENCE,
            userIndicator.plus("00".plus(mtnrSequence.toString()))
        )
        4 -> PreferenceHelper.save(
            Constants.MTNR_SEQUENCE,
            userIndicator.plus("0".plus(mtnrSequence.toString()))
        )
        5 -> PreferenceHelper.save(
            Constants.MTNR_SEQUENCE,
            userIndicator.plus(mtnrSequence.toString())
        )
    }
}

fun saveFgrnBatchSequence(currentBatchSequence: String) {
    val userIndicator = currentBatchSequence.substring(0, 1)
    val batchSequence = currentBatchSequence.substring(currentBatchSequence.length - 5).toInt().inc()
    //val batchSequence = currentBatchSequence.toInt().inc()
    when (batchSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.FGRN_BATCH_SEQUENCE, userIndicator.plus("0000").plus(batchSequence.toString())
        )
        2 -> PreferenceHelper.save(
            Constants.FGRN_BATCH_SEQUENCE, userIndicator.plus("000").plus(batchSequence.toString())
        )
        3 -> PreferenceHelper.save(
            Constants.FGRN_BATCH_SEQUENCE,userIndicator.plus("00").plus(batchSequence.toString())
        )
        4 -> PreferenceHelper.save(
            Constants.FGRN_BATCH_SEQUENCE, userIndicator.plus("0").plus(batchSequence.toString())
        )
        5 -> PreferenceHelper.save(Constants.FGRN_BATCH_SEQUENCE, userIndicator.plus(batchSequence.toString()))
    }
}

fun savePileSequence(currentBatchSequence: String) {
    val userIndicator = currentBatchSequence.substring(0, 1)
    val batchSequence = currentBatchSequence.substring(currentBatchSequence.length - 4).toInt().inc()
   // val batchSequence = currentBatchSequence.toInt().inc()
    when (batchSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.PILE_SEQUENCE, userIndicator.plus("000").plus(batchSequence.toString())
        )
        2 -> PreferenceHelper.save(
            Constants.PILE_SEQUENCE, userIndicator.plus("00").plus(batchSequence.toString())
        )
        3 -> PreferenceHelper.save(
            Constants.PILE_SEQUENCE,userIndicator.plus("0").plus(batchSequence.toString())
        )
        4 -> PreferenceHelper.save(
            Constants.PILE_SEQUENCE, userIndicator.plus(batchSequence.toString())
        )
        5 -> PreferenceHelper.save(Constants.PILE_SEQUENCE, batchSequence.toString())
    }
}


fun savePoSequence(currentPoSequence: String) {
    val userIndicator = currentPoSequence.substring(0, 1)
    val poSequence = currentPoSequence.substring(currentPoSequence.length - 5).toInt().inc()
    when (poSequence.toString().length) {
        1 -> PreferenceHelper.save(
            Constants.PO_SEQUENCE,
            userIndicator.plus("0000".plus(poSequence.toString()))
        )
        2 -> PreferenceHelper.save(
            Constants.PO_SEQUENCE,
            userIndicator.plus("000".plus(poSequence.toString()))
        )
        3 -> PreferenceHelper.save(
            Constants.PO_SEQUENCE,
            userIndicator.plus("00".plus(poSequence.toString()))
        )
        4 -> PreferenceHelper.save(
            Constants.PO_SEQUENCE,
            userIndicator.plus("0".plus(poSequence.toString()))
        )
        5 -> PreferenceHelper.save(Constants.PO_SEQUENCE, userIndicator.plus(poSequence.toString()))
    }
}

fun prepareAdvanceDetails(list: List<VegaNicaraguaGetAdvanceDetails>): List<VegaNicaraguaAdvanceDetails> {
    val advanceDetails = arrayListOf<VegaNicaraguaAdvanceDetails>()

    list.forEach {

        it.advanceCreationDetailsDTO!!.forEach { item ->
            if (item.deletedFlag == false) advanceDetails.add(item)
        }
    }
    return advanceDetails
}

fun PrepareInvoiceReceiptData(
    receipt: List<VegaNicInvoiceReceipt>,
    supplierList: ArrayList<VegaVendor>
): ArrayList<VegaNicaraguaInvoiceDetails> {
    val invoiceList = ArrayList<VegaNicaraguaInvoiceDetails>()
    receipt.forEach {
        if (it.invoiceDBDetails == null) return@forEach
        val invoice = VegaNicaraguaInvoiceDetails()
        invoice.tempId = it.invoiceNumber.toString()
        invoice.currency = it.currency.toString()
        invoice.charg = it.invoiceDBDetails?.batchNumber
        invoice.materialName = it.invoiceDBDetails?.materialName
        invoice.materialNumber = it.invoiceDBDetails?.materialCode
        invoice.supplierName = it.invoiceDBDetails?.vendorName
        invoice.supplierCode = it.invoiceDBDetails?.vendorCode
        invoice.basePrice = it.invoiceDBDetails?.poPrice
        invoice.totalPrice = it.invoiceDBDetails?.totalPrice
        invoice.grnType = it.invoiceDBDetails?.grnType
        invoice.grn = it.invoiceDBDetails?.grnNumber
        invoice.grnQty = it.invoiceDBDetails?.grnNetWeight
        invoice.erdat = it.invoiceDBDetails?.invoiceDate
        invoice.qualityGrade = it.invoiceDBDetails?.qualityGradeDesc
        invoice.netPayment = it.invoiceDBDetails?.netPayment
        invoice.taxId = getTaxIdFromVendorList(supplierList, it.invoiceDBDetails?.vendorCode)
        invoice.bagsCount = it.invoiceDBDetails?.bagCount
        invoice.grossWeight = it.invoiceDBDetails?.grossWeight
        if (it.invoiceDBDetails?.grnType?.contains("ptbf", true) == true)
            invoice.tareWeight = it.invoiceDBDetails?.tareWeight
        else
            invoice.tareWeight =
                (if (it.invoiceDBDetails?.bagCount?.isNotEmpty() == true) it.invoiceDBDetails?.bagCount?.toInt()
                    ?: 0 else 0).times(
                    if (it.invoiceDBDetails?.tareWeight?.isNotEmpty() == true) it.invoiceDBDetails?.tareWeight?.toDouble()
                        ?: 0.0 else 0.0
                ).formatTwoDigits()
//            it.invoiceDBDetails?.tareWeight
        invoice.totalFTDC = it.invoiceDBDetails?.ftdc
        invoice.totalVolumePremium = it.invoiceDBDetails?.volumePremium
        invoice.invoiceNo = it.invoiceDBDetails?.tempNumber
        //New fields
        invoice.isReceiptData = true
        invoice.netWeight = it.invoiceDBDetails?.netWeight
        invoice.certificatePremium = it.invoiceDBDetails?.certificatePremium
        invoice.volumePremium = it.invoiceDBDetails?.volumePremium
        invoice.humidityPremium = it.invoiceDBDetails?.humidityPremium
        invoice.qualityDiscounting = it.invoiceDBDetails?.qualityPremium
        invoice.humidityDiscounting = it.invoiceDBDetails?.humidityDiscounting
        invoice.grossValue = it.invoiceDBDetails?.grossValue
        invoice.exportnCentives = it.invoiceDBDetails?.exportIncentive
        invoice.withholdingTax = it.invoiceDBDetails?.withHoldingTax
        invoice.NSExchangeRate = it.invoiceDBDetails?.nationalStockExchange
        invoice.bankCommission = it.invoiceDBDetails?.bankCommission
        invoice.FTDC = it.invoiceDBDetails?.ftdc
        invoice.totalDduction = it.invoiceDBDetails?.subTotalDeductions
        invoice.finalPayment = it.invoiceDBDetails?.finalPayment
        invoice.grossValuePerKg = String.format(
            Locale.ENGLISH, "%.2f",
            covertToDouble(
                removeDollerInValue(it.invoiceDBDetails?.grossValue.toString())
            ) / covertToDouble(it.invoiceDBDetails?.netWeight.toString())
        )
        invoice.qualityGradeDesc = it.invoiceDBDetails?.qualityGradeDesc
        invoice.netWeightQQs =
            String.format(
                Locale.ENGLISH,
                "%.2f",
                covertToDouble(it.invoiceDBDetails?.netWeight.toString()) / 46
            )
        invoice.exchangeRate = it.invoiceDBDetails?.exchangeRate
        if (it.invoiceDBDetails?.advanceLineItems?.size ?: 0 > 0) {
            var advanceSummary: Double = 0.0
            var interestSummary: Double = 0.0
            var commissionSummary: Double = 0.0
            var legalExpenseSummary: Double = 0.0
            var totalAdvanceSummary: Double = 0.0
            var currencyDevaluationSummary: Double = 0.0

            it.invoiceDBDetails?.advanceLineItems?.forEach {
                advanceSummary = covertToDouble(it.knockOffAmt!!) + advanceSummary
                interestSummary = covertToDouble(it.interest!!) + interestSummary
                commissionSummary = covertToDouble(it.interestCommission!!) + commissionSummary
                legalExpenseSummary = covertToDouble(it.legalExpense!!) + legalExpenseSummary
                totalAdvanceSummary = covertToDouble(it.grossValue!!) + totalAdvanceSummary
                currencyDevaluationSummary = covertToDouble(it.currencyDevaluation!!) + currencyDevaluationSummary
            }

            invoice.advanceSummary = advanceSummary.toString()
            invoice.advanceInterestSummary = interestSummary.toString()
            invoice.advanceCommissionSummary = commissionSummary.toString()
            invoice.advanceLegalExpenseSummary = legalExpenseSummary.toString()
            invoice.advanceMaintainceSummary = currencyDevaluationSummary.toString()
            invoice.totalAdvanceSummary = totalAdvanceSummary.toString()
            invoice.advanceItems = prepareAdvanceLineItem(it.invoiceDBDetails?.advanceLineItems)

        }
        invoiceList.add(invoice)
    }
    return invoiceList
}

fun prepareAdvanceLineItem(advanceLineItems: List<com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaAdvanceLineItems>?): List<VegaNicaraguaAdvanceLineItemGrn> {
    val advanList = arrayListOf<VegaNicaraguaAdvanceLineItemGrn>()
    advanceLineItems?.forEach {
        val advan = VegaNicaraguaAdvanceLineItemGrn()
        advan.documentNumber = it.advanceDocumentNumber.toString()
        advan.advanceKnockAmount = it.knockOffAmt
        advan.totalAdvanceKnockAmount = it.grossValue
        advan.interestAmount = it.interest
        advan.commissionAmount = it.interestCommission
        advan.legalExpenseAmount = it.legalExpense
        advan.currencyDevaluationAmount = it.currencyDevaluation
        advanList.add(advan)
    }
    return advanList
}

fun PrepareGrnReceiptData(
    receiptList: List<VegaReceiving>,
    supplierList: ArrayList<VegaVendor>,
    materialList: ArrayList<VegaMaterial>,
    qualityGrade: ArrayList<VegaNicaraguaMaterialQualitGrades>
): List<VegaReceiving> {
    receiptList.forEachIndexed { index, it ->
        it.status = Status.SYNC_REPRINT
        it.tmpWbId = it.grnNumber ?: ""
        it.supplierName = getVendorNameFromVendorList(supplierList, it.supplierCode.toString())
        it.materialName = getMaterialNameFromMaterialList(
            materialList.toMutableList(),
            it.materialCode.toString()
        )
        it.taxId = getTaxIdFromVendorList(supplierList, it.supplierCode.toString())
        it.gradeDesc =
            (if (it.inventoryDTO?.isNotEmpty() == true) getQualityDescriptionFromCode(
                it.inventoryDTO?.get(0)?.gradeDesc, qualityGrade
            ) else "").toString()
        it.certificate =
            (if (it.inventoryDTO?.isNotEmpty() == true) it.inventoryDTO?.get(0)?.certification else "").toString()
    }
    return receiptList
}


private fun removeDollerInValue(value: String): String {

    var text = value.replace("C$", "").trim()

    return text
}

fun getVendorNameFromVendorList(
    supplierList: java.util.ArrayList<VegaVendor>,
    vendorCode: String?
): String? {
    val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
    return if (vendor.size > 0) vendor[0].vendorName else ""
}

private fun getMaterialNameFromMaterialList(
    materialList: MutableList<VegaMaterial>,
    materialCode: String
): String? {
    val material = materialList.filter { materialCode.contains(it.materialCode) }
    return if (material.size > 0) material[0].materialName else ""
}

private fun getQualityDescriptionFromCode(
    gradeCode1: String?,
    qualityGrade: ArrayList<VegaNicaraguaMaterialQualitGrades>
): String? {
    val gradeCode = if (gradeCode1?.length ?: 0 >= 4) gradeCode1?.takeLast(4).toString() else ""
    val desc = qualityGrade.filter { it.gradeCode.contains(gradeCode) }
    return if (desc.isNotEmpty()) desc[0].grade else ""
}

fun covertToDouble(value: String?): Double {
    if (value != null && value != "null" && value.length > 0) {
        val str = value.format(Locale.ENGLISH).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}


fun getTaxIdFromVendorList(
    supplierList: java.util.ArrayList<VegaVendor>,
    vendorCode: String?
): String? {
    val vendor = supplierList.filter { vendorCode?.contains(it.vendorCode) ?: false }
    return if (vendor.size > 0) vendor[0].taxNumber else ""
}

fun prepareWeighBridgeData(receiving: VegaReceiving): VegaQualityWBDetails {
    val qualityWB = VegaQualityWBDetails()
    qualityWB.weighBridgeId =
        if (receiving.weighBridgeId.isNotEmpty()) receiving.weighBridgeId.toString() else receiving.tmpWbId
    qualityWB.wbTempId = receiving.tmpWbId
    qualityWB.item = receiving.item
    qualityWB.direction = receiving.direction.toString()
    qualityWB.weighBridgeType = receiving.weighBridgeType.toString()
    qualityWB.plant = receiving.plantId
    qualityWB.batchNumber = receiving.batchNumber
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (receiving.materialCode?.length != 18) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = receiving.supplierCode
    qualityWB.supplierName = receiving.supplierName
    qualityWB.bagCount = receiving.bagCount
    qualityWB.grnNumber = receiving.grnNumber.toString()
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.deliveryItem
    qualityWB.netWeight = receiving.netWeight.toString()
    qualityWB.grossWeight = receiving.grossWeight.toString()
    qualityWB.unitsOfMeasure = receiving.unitsOfMeasure.toString()
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")

    return qualityWB
}

fun prepareWeighBridgeData(receiving: DOReceiving): DOQualityWBDetails {
    val qualityWB = DOQualityWBDetails()
    qualityWB.weighBridgeId = receiving.tmpWbId
    qualityWB.wbTempId = receiving.tmpWbId
    qualityWB.item = receiving.item
    qualityWB.plant = receiving.plantId
    qualityWB.batchNumber = receiving.charg
    qualityWB.materialName = receiving.materialName
    qualityWB.materialCode =
        if (PreferenceHelper.get(
                Constants.COUNTRY_CODE,
                ""
            ) != CountryCode.VIETNAM.code && receiving.materialCode?.length != 18
        ) "000000".plus(receiving.materialCode) else receiving.materialCode
    qualityWB.supplierCode = receiving.supplierCode
    qualityWB.bagCount = receiving.bagCount
    qualityWB.bagType = receiving.bagType
    qualityWB.bagWeight = receiving.bagWeight.toString()
    qualityWB.deliveryItem = receiving.posnr
    qualityWB.netWeight = receiving.netWeight.toString()
    qualityWB.isNotWBID = true
    qualityWB.erdat = "/Date(".plus(DateUtils.getCurrentTimeInMills()).plus(")/")
    qualityWB.challan = receiving.txnId
    qualityWB.grossWeight = receiving.grossWeight.toString()
    qualityWB.unitsOfMeasure = receiving.uom
    qualityWB.currentKey = receiving.currentKey
    return qualityWB
}

fun prepareFeatureMater(item: List<VegaFeatureMaster>): List<VegaFeatureMaster> {
    resetTrackTraceFeatureMaster()
    val fieldList = arrayListOf<VegaFeatureMaster>()
    try {
        item.forEach {
            val fieldItem = VegaFeatureMaster()
            /*if (it.moduleName.equals("Honduras", true) ||
                it.moduleName.equals("Guatemala",true) ||
                it.moduleName.equals("Peru", true)  ||
                it.moduleName.equals("Vietnam", true)||
                it.moduleName.equals("Brazil",true)  ||
                it.moduleName.equals("Indo",true) ||
                it.moduleName.equals("Uganda",true)||
                it.moduleName.equals("India",true)||
                it.moduleName.equals("Mexico",true)||
                it.moduleName.equals("Columbia",true)||
                it.moduleName.equals("Ghana",true)
            ) {
                when (it.featureName) {
                    "UOM" -> PreferenceHelper.save(CURRENT_UOM, it.featureValue ?: "")
                    "Receiving UOM" ->PreferenceHelper.save(UG_RECEIVING_UOM, it.featureValue ?: "")
                    "SingleLevelQcApproval" -> PreferenceHelper.save(SINGLE_QC_APPROVAL,it.mandatory ?: false)
                    "Bag Return" -> {
                        if(it.mandatory == true) PreferenceHelper.save(BAG_RETURN, it.featureValue.toString())
                    }
                }
            }*/
            if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.DIRECT)){
                PreferenceHelper.save(Constants.DIRECT, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.IN_DIRECT)){
                PreferenceHelper.save(Constants.IN_DIRECT, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.TRACK_TRACE_THIRD_PARTY, true) && it.mandatory==true){
                PreferenceHelper.save(Constants.TRACK_TRACE_THIRD_PARTY, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION, true) && it.mandatory==true){
                PreferenceHelper.save(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION_ID, true) && it.mandatory == true){
                PreferenceHelper.save(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION_ID, it.featureName)
            } else if(it.moduleName.equals(Constants.SOURCE_LOT) && it.featureName.equals(Constants.SOURCE_LOT_SYNC, true) && it.mandatory==true){
                PreferenceHelper.save(Constants.SOURCE_LOT_SYNC, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.CROP_LIMIT, true) && it.mandatory == true){
                PreferenceHelper.save(Constants.CROP_LIMIT, it.featureName)
            } else if(it.moduleName.equals(Constants.TRACK_TRACE) && it.featureName.equals(Constants.TRANS_ID_SYNC, true) && it.mandatory==true){
                PreferenceHelper.save(Constants.TRANS_ID_SYNC, it.featureName)
            }
            fieldItem.dataType = it.dataType
            fieldItem.featureDesc = it.featureDesc
            fieldItem.featureName = it.featureName.trim()
            fieldItem.fieldType = it.fieldType
            fieldItem.fieldValue = it.fieldValue?.trim()
            fieldItem.featureValue = it.featureValue?.trim()
            fieldItem.mandatory = it.mandatory
            fieldItem.moduleDesc = it.moduleDesc
            fieldItem.moduleName = it.moduleName.trim()
            fieldItem.subModuleName =
                if (it.subModuleName.isNullOrEmpty()) "" else it.subModuleName.trim()
            fieldItem.subModuleDesc = it.subModuleDesc ?: ""
            fieldItem.screenName = it.screenName?.trim()
            fieldItem.screenDesc = it.screenDesc
            fieldItem.visible = it.visible
            fieldList.add(fieldItem)
        }

    }catch (e:Exception){e.printStackTrace()}
    return fieldList
}

fun resetTrackTraceFeatureMaster(){
        PreferenceHelper.save(Constants.DIRECT, "")
        PreferenceHelper.save(Constants.IN_DIRECT, "")
        PreferenceHelper.save(Constants.TRACK_TRACE_THIRD_PARTY, "")
        PreferenceHelper.save(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION, "")
        PreferenceHelper.save(Constants.TRACK_TRACE_FARMERLESS_TRANSACTION_ID, "")
        PreferenceHelper.save(Constants.SOURCE_LOT_SYNC,"")
        PreferenceHelper.save(Constants.TRANS_ID_SYNC, "")

}

fun prepareFarmerLessTransactionDetails(it1: List<TrackTraceModelTransactionIdDetails>) : List<TrackTraceTransactionIdDetails>{
    val transactionList = arrayListOf<TrackTraceTransactionIdDetails>()
    it1.forEach { it ->
        val transaction = TrackTraceTransactionIdDetails()
        transaction.dwTransactionId = it.dwTransactionId
        transaction.vendorName = it.vendorDetails.get(0).vendorName
        transaction.vendorCode = it.vendorDetails.get(0).vendorCode
        transaction.country = it.vendorDetails.get(0).country
        transaction.product = it.vendorDetails.get(0).product
        transaction.totalProductionInMetricTon = it.vendorDetails.get(0).totalProductionInMetricTon
        transaction.dateGeoLocationCaptured = it.vendorDetails.get(0).dateGeoLocationCaptured
        transaction.compliantFlag = it.vendorDetails.get(0).compliantFlag
        transactionList.add(transaction)
    }
    return transactionList
}
