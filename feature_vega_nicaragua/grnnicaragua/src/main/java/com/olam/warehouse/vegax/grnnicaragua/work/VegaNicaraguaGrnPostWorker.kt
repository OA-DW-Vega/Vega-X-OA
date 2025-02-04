package com.olam.warehouse.vegax.grnnicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.master.veganicaragua.model.*
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.GRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.GRN_OUTPUT_DATA
import com.olam.warehouse.vegax.grnnicaragua.data.api.VegaNicaraguaGrnApi
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaGlDetailsModel
import com.olam.warehouse.vegax.grnnicaragua.utils.GL_DETAILS
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import com.olam.warehouse.vegax.grnnicaragua.utils.prepareQualityParamData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaNicaraguaGrnPostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaGrnApi by inject()
        val dao: VegaNicaraguaGrnDao by inject()
        val wbid = inputData.getString(GRN_DATA) ?: ""
        val receivingData = dao.getReceivingData(wbid)
        val qualityList = dao.getQualityParams(wbid)
        val advanceValues = dao.getAdvanceItemById(wbid)
        val qualityData = prepareQualityParamData(qualityList)
        val bagItems =
            dao.getBagItemsWorker(/*receivingData.materialCode, receivingData.supplierCode.toString(), */receivingData.tmpWbId)
        val code = receivingData.grade?.replace("   ", " ")?.split(" ")?.get(1) ?: ""
        val gradeMappingDescription = dao.getGradeMappingWorker(code).description.toString()
        val priceDetails = getFilteredPriceDetails(
            dao.getGrnPriceDetailsWorker() as ArrayList<VegaNicaraguaGrnPriceDetails>,
            gradeMappingDescription,
            receivingData
        )
        val exchangeData = dao.getExchangeRateOfflineWorker(getCurrentKey())
        val exchangeRate = if (exchangeData != null) exchangeData.exchangeRate else ""
        val usdAmount = if (exchangeData != null) exchangeData.currencyValue else ""
        val currency = if (exchangeData != null) exchangeData.currencyCode?.toString() else ""
        val priceConfig = dao.getPriceConfigInfoWorker(
            receivingData.materialCode.toString(),
            receivingData.grade!!.substring(receivingData.grade!!.length - 4)
        )
        var tollingDetails = arrayListOf<VegaNicaraguaTollingDetails>()
        var jsonData = mutableListOf<String>()
        val tolling = dao.getGlDetailsWork(getCurrentKey())
        tolling.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        jsonData.forEach {
            if (it.contains(GL_DETAILS)) {
                val details = gson.fromJson(it, VegaNicaraguaGlDetailsModel::class.java)
                if (details.GL_DETAILS.isNotEmpty()) {
                    details.GL_DETAILS.forEach {
                        var toll = VegaNicaraguaTollingDetails()
                        toll.costCenter = it.COST_CENTER
                        toll.glAccount = it.GL_ACCOUNT
                        tollingDetails.add(toll)
                    }
                }
            }
        }

        val post = VegaNicaraguaGrnPost()
        post.batchNumber = receivingData.batchNumber
        post.wbId = receivingData.weighBridgeId
        post.certificate = receivingData.certificate
        post.qualityGradeDesc = receivingData.gradeDesc
        post.cascara = receivingData.cascara
        post.humedad = receivingData.humedad
        post.rendimientoBruto = receivingData.rendimientoBruto
        post.exchangeRate = receivingData.exchangeRate
        post.grade = null
        post.key = getCurrentKey()
        post.grnType = receivingData.grnType
        post.plant = getPlantDetails()
        post.grnData = preparingGrnData(receivingData, currency, bagItems)
        post.lotDetails = preparingLotDetails(receivingData, qualityList)
        post.priceDetails = priceDetails
        post.tollingDetails = if (receivingData.grnType.equals("spot-tolling")) tollingDetails else null
        post.wbFlag = receivingData.wbFlag
        post.qcFlag = receivingData.qcFlag
        post.grnFlag = receivingData.grnFlag
        post.poNumber = receivingData.purchaseDocNum
        post.tempGRNNumber = receivingData.palletType
        post.grnNumber = receivingData.grnNumber
        if (!receivingData.grnType.equals("ptbf", true)) {
            post.invoiceDetails =
                preparingInvoiceData(advanceValues, priceConfig, receivingData, exchangeRate!!, usdAmount!!, bagItems)
        }
        if (receivingData.wbFlag == false && receivingData.qcFlag == false && receivingData.grnFlag == false)
            post.transactionMode = "OFF"
        else {
            if (receivingData.direction.equals("Offline"))
                post.transactionMode = "OFF-Retry"
            else
                post.transactionMode = "ON-Retry"
        }
        //adding weighDetails
        post.weighDetails = bagItems
        /*if (receivingData.syncStarted == 1) {
            Result.failure(workDataOf(GRN_OUTPUT_DATA to "msg"))
        } else {*/
            try {
                dao.updateSyncStartedStatus(wbid)
                val response = api.syncGrnData(post).execute()
                if (response.isSuccessful) {
                    val resp = response.body()
                    val respData = response.body()?.data
                    if (respData != null) {
                        if (resp?.data?.wbFlag == true && resp.data.qcFlag == true && resp.data.grnFlag == true) {
                            if (receivingData.grnType.equals("ptbf", true)) {
                                receivingData.weighBridgeId = resp.data.wbId.toString()
                                receivingData.batchNumber = resp.data.batchNumber.toString()
                                receivingData.grnNumber = resp.data.grnNumber.toString()
                                receivingData.purchaseDocNum = resp.data.poNumber.toString()
                                receivingData.status = Status.SYNC_COMPLETED
                                receivingData.isSynced = true
                            } else if (receivingData.grnType?.contains("tolling", true) == true) {
                                receivingData.weighBridgeId = resp.data.wbId.toString()
                                receivingData.batchNumber = resp.data.batchNumber.toString()
                                receivingData.grnNumber = resp.data.grnNumber.toString()
                                receivingData.purchaseDocNum = resp.data.poNumber.toString()
                                receivingData.status = Status.SYNC_COMPLETED
                                receivingData.isSynced = true
                            } else {
                                if (resp.data.invoiceFlag == true) {
                                    receivingData.weighBridgeId = resp.data.wbId.toString()
                                    receivingData.batchNumber = resp.data.batchNumber.toString()
                                    receivingData.grnNumber = resp.data.grnNumber.toString()
                                    receivingData.purchaseDocNum = resp.data.poNumber.toString()
                                    receivingData.status = Status.SYNC_COMPLETED
                                    receivingData.isSynced = true
                                }else {
                                    receivingData.weighBridgeId = resp.data.wbId.toString()
                                    receivingData.batchNumber = resp.data.batchNumber.toString()
                                    receivingData.purchaseDocNum = resp.data.poNumber.toString()
                                    receivingData.grnNumber = resp.data.grnNumber.toString()
                                    receivingData.status = Status.SYNC_PENDING
                                    receivingData.isSynced = false
                                    receivingData.syncStatusMsg = resp.message.toString()
                                }
                            }
                        } else {
                            receivingData.weighBridgeId = resp?.data?.wbId.toString()
                            receivingData.batchNumber = resp?.data?.batchNumber.toString()
                            receivingData.purchaseDocNum = resp?.data?.poNumber.toString()
                            receivingData.status = Status.SYNC_PENDING
                            receivingData.isSynced = false
                            receivingData.syncStatusMsg = resp?.message.toString()
                        }
                        receivingData.wbFlag = resp?.data?.wbFlag ?: false
                        receivingData.qcFlag = resp?.data?.qcFlag ?: false
                        receivingData.grnFlag = resp?.data?.grnFlag ?: false
                        receivingData.invoiceFlag=resp?.data?.invoiceFlag?:false
                        dao.saveGrnData(receivingData)
                        Result.success(workDataOf(GRN_OUTPUT_DATA to resp?.data?.wbId))
                    } else {
                        val msg = resp?.message ?: resp?.errors
                        dao.updateErrorGrnData(wbid, msg.toString())
                        Result.failure(workDataOf(GRN_OUTPUT_DATA to msg))
                    }
                } else {
                    val msg = response.message()
                    dao.updateErrorGrnData(wbid, msg.toString())
                    Result.failure(workDataOf(GRN_OUTPUT_DATA to response.message()))
                }

            } catch (error: Throwable) {
                Result.failure(workDataOf(GRN_OUTPUT_DATA to error.message))
            }
//        }
    }
}
private fun prepareAdvanceLineItems(advanceKnockOfList: List<VegaNicaraguaAdvanceLineItemGrn>): java.util.ArrayList<VegaNicaraguaAdvanceLineItems> {
    var advanceLineItems = java.util.ArrayList<VegaNicaraguaAdvanceLineItems>()

    advanceKnockOfList.forEach {
        var advanceItem = VegaNicaraguaAdvanceLineItems()
        advanceItem.advanceAmount = it.amount
        advanceItem.itemNum = it.itemNum
        advanceItem.interest = it.interestAmount
        advanceItem.legalExpense = it.legalExpenseAmount
        advanceItem.advanceDate = it.documentDate
        advanceItem.advanceDocumentNumber = it.documentNumber
        advanceItem.currencyDevaluation = it.currencyDevaluationAmount
        advanceItem.interestCommission = it.interestAmount
        advanceItem.knockOffAmt = it.advanceKnockAmount
        advanceItem.grossValue = it.totalAdvanceKnockAmount
        advanceLineItems.add(advanceItem)
    }

    return advanceLineItems
}

private fun preparingInvoiceData(
    advanceValues: List<VegaNicaraguaAdvanceLineItemGrn>,
    priceConfigDetails: VegaNicaraguaPriceConfigDetails,
    receivingData: VegaReceiving,
    exchangeRate: String,
    USDAmount: String,
    bagItems: List<VegaNicaraguaWeighmentBagMaterial>
): java.util.ArrayList<VegaNicaraguaInvoiceDetails> {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
   /* var certificatePremium: Double? = 0.0
    var volumePremium: Double? = 0.0
    var humidityPremium: Double? = 0.0
    var qualityDiscount: Double? = 0.0
    var totalPrice: Double? = 0.0
    var grossValue: Double = 0.0
    var netPayment: Double? = 0.0
    var netWeight = receivingData.netWeight*/
    var invoiceList = java.util.ArrayList<VegaNicaraguaInvoiceDetails>()
    if (priceConfigDetails != null) {
        try {
            var advance = 0.0
            if (advanceValues.size > 0) {
                advanceValues.forEach {
                    advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                }
            }

           /* totalPrice = covertToDouble(priceConfigDetails.dailyPrice) * netWeight.toDouble()
            certificatePremium = covertToDouble(priceConfigDetails.certificatePremium) * netWeight.toDouble()
            var volumeTo = covertToDouble(priceConfigDetails.volumePremiumTo)
            var volumeFrom = covertToDouble(priceConfigDetails.volumePremiumFrom)

            if (volumeFrom > 0 && volumeTo > 0) {
                if (netWeight.toDouble() >= volumeFrom && netWeight.toDouble() <= volumeTo) {
                    volumePremium =
                        covertToDouble(priceConfigDetails.volumePremium) * netWeight.toDouble()
                }
            } else if (volumeFrom > 0) {
                if (netWeight.toDouble() >= volumeFrom) {
                    volumePremium =
                        covertToDouble(priceConfigDetails.volumePremium) * netWeight.toDouble()
                }
            } else if (volumeTo > 0) {
                if (netWeight.toDouble() <= volumeTo) {
                    volumePremium =
                        covertToDouble(priceConfigDetails.volumePremium) * netWeight.toDouble()
                }
            } else {
                volumePremium = 0.00
            }
            humidityPremium = covertToDouble(priceConfigDetails.moisturePremium) * netWeight.toDouble()
            qualityDiscount = covertToDouble(priceConfigDetails.qualityDiscount) * netWeight.toDouble()
            val humidityDiscount: Double =
                (totalPrice + certificatePremium + volumePremium!! + humidityPremium) * (covertToDouble(
                    priceConfigDetails.moistureDiscount!!
                ) / 100)
            grossValue =
                (totalPrice + certificatePremium + volumePremium + humidityPremium) - (qualityDiscount + humidityDiscount)
            val exportIncentive: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.exportIncentive!!) / 100)
            val withHoldingTax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.withHoldingTax!!) / 100)
            val NSETax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.neCommission!!) / 100)
            val commissionTax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.bankCommsion!!) / 100)
            val ftdc: Double =
                ((USDAmount.toDouble()) * (exchangeRate.toDouble()) * (receivingData.yieldPercentage!!.toDouble() / 100) * netWeight.toDouble()) / 46
            val totalDeductions = withHoldingTax + commissionTax + NSETax + ftdc
            netPayment = grossValue + exportIncentive - totalDeductions*/

            var advanceSummary:Double=0.0
            var interestSummary:Double=0.0
            var commissionSummary:Double=0.0
            var legalExpenseSummary:Double=0.0
            var currencyDevaluationSummary:Double=0.0
            var totalAdvanceSummary:Double=0.0

            if (advanceValues.size > 0) {

                advanceValues.forEach {
                    advanceSummary = covertToDouble(it.advanceKnockAmount!!) + advanceSummary
                    interestSummary = covertToDouble(it.interestAmount!!) + interestSummary
                    commissionSummary = covertToDouble(it.commissionAmount!!) + commissionSummary
                    legalExpenseSummary = covertToDouble(it.legalExpenseAmount!!) + legalExpenseSummary
                    totalAdvanceSummary = covertToDouble(it.totalAdvanceKnockAmount!!) + totalAdvanceSummary
                    currencyDevaluationSummary =
                        covertToDouble(it.currencyDevaluationAmount!!) + currencyDevaluationSummary
                }
            }

            val info = VegaNicaraguaInvoiceDetails()
            /*info.advanceKnockOffAmt = String.format(Locale.ENGLISH, "%.2f", advanceSummary)
            info.advanceLineItems = prepareAdvanceLineItems(advanceValues)
            info.poPrice = priceConfigDetails.dailyPrice
            info.netWeight = netWeight
            info.totalPrice = String.format(Locale.ENGLISH, "%.2f", totalPrice)
            info.certificatePremium = String.format(Locale.ENGLISH, "%.2f", certificatePremium)
            info.volumePremium = String.format(Locale.ENGLISH, "%.2f", volumePremium)
            info.humidityPremium = String.format(Locale.ENGLISH, "%.2f", humidityPremium)
            info.qualityPremium = String.format(Locale.ENGLISH, "%.2f", qualityDiscount)
            info.humidityDiscounting = String.format(Locale.ENGLISH, "%.2f", humidityDiscount)
            info.exportIncentive = String.format(Locale.ENGLISH, "%.2f", exportIncentive)
            info.withHoldingTax = String.format(Locale.ENGLISH, "%.2f", withHoldingTax)
            info.nationalStockExchange = String.format(Locale.ENGLISH, "%.2f", NSETax)
            info.bankCommission = String.format(Locale.ENGLISH, "%.2f", commissionTax)
            info.ftdc = String.format(Locale.ENGLISH, "%.2f", ftdc)
            info.subTotalDeductions = String.format(Locale.ENGLISH, "%.2f", totalDeductions)
            info.netPayment = String.format(Locale.ENGLISH, "%.2f", netPayment)
            info.finalPayment = String.format(Locale.ENGLISH, "%.2f", (netPayment - advance))
            info.grossValue = String.format(Locale.ENGLISH, "%.2f", grossValue)
            info.key = currentKey
            info.exchangeRate = exchangeRate*/
            info.advanceKnockOffAmt = receivingData.advanceSummary
            info.advanceLineItems = prepareAdvanceLineItems(advanceValues)
            info.poPrice = receivingData.basePrice
            info.grnNetWeight = receivingData.netWeight
            info.totalPrice = receivingData.totalPrice
            info.certificatePremium = receivingData.certificatePremium
            info.volumePremium = receivingData.volumePremium
            info.humidityPremium = receivingData.humidityPremium
            info.qualityPremium = receivingData.qualityDiscounting
            info.humidityDiscounting = receivingData.humidityDiscounting
            info.exportIncentive = receivingData.exportnCentives
            info.withHoldingTax = receivingData.withholdingTax
            info.nationalStockExchange = receivingData.NSExchangeRate
            info.bankCommission = receivingData.bankCommission
            info.ftdc = receivingData.FTDC
            info.subTotalDeductions = receivingData.totalDduction
            info.netPayment = receivingData.netPayment
            info.finalPayment = receivingData.finalPayment
            info.grossValue = receivingData.grossValue
            info.key = currentKey
            info.exchangeRate = receivingData.exchangeRate
            info.grade = receivingData.grade
            info.qualityGradeDesc = receivingData.gradeDesc
            info.vendorCode = receivingData.supplierCode
            info.vendorName = receivingData.supplierName
            info.tempNumber = receivingData.invoiceNumber
            info.storageLocationCode = receivingData.storageLocationCode
            info.storageLocationName = receivingData.storageLocationName
            info.plant = getPlantDetails()
            info.netWeight = receivingData.netWeight
            info.materialCode = receivingData.materialCode
            info.materialName = receivingData.materialName
            info.grnType = receivingData.grnType
            info.bagCount =
                bagItems.sumBy { if (it.bagCount.isNotEmpty()) it.bagCount.toInt() else 0 }.toString()
            info.grossWeight =
                bagItems.sumByDouble { if (it.grossWeight.isNotEmpty()) it.grossWeight.toDouble() else 0.0 }.toString()
            info.tareWeight = bagItems.sumByDouble {
                if (it.tareWeight?.isNotEmpty() == true) it.tareWeight?.toDouble() ?: 0.0 else 0.0
            }.toString()
            info.batchNumber = receivingData.batchNumber
            info.certification = receivingData.certificate
            info.currency = if (receivingData.currency?.isNotEmpty() == true) receivingData.currency else "NIO"
            info.uom = receivingData.unitsOfMeasure
            info.poNumber = receivingData.purchaseDocNum
            info.invoiceDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

            invoiceList.add(info)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    return invoiceList
}

private fun preparingGrnData(
    receivingData: VegaReceiving,
    currency: String?,
    bagItems: List<VegaNicaraguaWeighmentBagMaterial>
): ArrayList<VegaNicaraguaGrnData> {
    val grnDataList = ArrayList<VegaNicaraguaGrnData>()
    val grnData = VegaNicaraguaGrnData()
    grnData.batchNumber = receivingData.batchNumber
    grnData.currency = if (receivingData.currency?.isNotEmpty() == true) receivingData.currency else "NIO"
    grnData.price = receivingData.price
    val weight = bagItems.sumByDouble { if (it.netWeight.isNotEmpty()) it.netWeight.toDouble() else 0.0 }
    grnData.netWeight = weight.toString()
//    grnData.netWeight = receivingData.netWeight
    grnData.materialCode = receivingData.materialCode
    grnData.supplierCode = receivingData.supplierCode
    grnData.weighBridgeType = receivingData.weighBridgeType
    grnData.weighBridgeId = receivingData.weighBridgeId
    grnData.unitsOfMeasure = receivingData.unitsOfMeasure
    grnData.purchaseOrderNum = receivingData.purchaseDocNum
    grnData.storageLocationCode = receivingData.storageLocationCode
    grnData.plant = receivingData.plantId
    grnData.item = receivingData.item
    grnData.createdDate =
        if (!receivingData.createdDate.isNullOrEmpty()) receivingData.createdDate else DateUtils.getDate(
            Calendar.getInstance().timeInMillis,
            "yyyyMMdd"
        )
    grnData.docDate = if (!receivingData.docDate.isNullOrEmpty()) receivingData.docDate else DateUtils.getDate(
        Calendar.getInstance().timeInMillis,
        "yyyyMMdd"
    )

    grnDataList.add(grnData)

    return grnDataList
}

private fun preparingLotDetails(
    receivingData: VegaReceiving,
    qualityList: List<VegaQuality>
): ArrayList<VegaNicaraguaLotDetails> {
    val lotDetailsList = ArrayList<VegaNicaraguaLotDetails>()
    val lotDetails = VegaNicaraguaLotDetails()

    lotDetails.batchNumber = receivingData.batchNumber
    lotDetails.netWeight = receivingData.netWeight
    lotDetails.materialCode = receivingData.materialCode
    lotDetails.supplierCode = receivingData.supplierCode
    lotDetails.weighBridgeType = receivingData.weighBridgeType
    lotDetails.weighBridgeId = receivingData.weighBridgeId
    lotDetails.unitsOfMeasure = receivingData.unitsOfMeasure
    lotDetails.purchaseDocNum = receivingData.purchaseDocNum
    lotDetails.storageLocationCode = receivingData.storageLocationCode
    lotDetails.storageLocation = receivingData.storageLocationName
    lotDetails.grossWeight = receivingData.grossWeight
    lotDetails.bagType = receivingData.bagType
    lotDetails.plant = receivingData.plantId
    lotDetails.item = receivingData.item
    //adding quality details to lot
    val qualityDetailsList = ArrayList<VegaNicaraguaQualityDetails>()
    qualityList.forEach {

        val qualityDetail = VegaNicaraguaQualityDetails()
        qualityDetail.descrChar = it.descrChar
        qualityDetail.nameChar = it.nameChar
        qualityDetail.qualityParameterValue = it.qualityParameterValue
        qualityDetailsList.add(qualityDetail)
    }

    lotDetails.qualityDetails = qualityDetailsList
    lotDetailsList.add(lotDetails)

    return lotDetailsList
}

private fun getFilteredPriceDetails(
    priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>,
    gradeMappingDescription: String,
    receivingData: VegaReceiving
): ArrayList<VegaNicaraguaGrnPriceDetails> {
    var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

    priceDetails.forEach {
        if (it.fieldName.equals("LOWGRD01", true)) {
            it.percentage = receivingData.DESMA
            filteredPriceDetails.add(it)
        } else if ("LOWGRD02".equals(it.fieldName, true)) {
            it.percentage = receivingData.DESMC
            filteredPriceDetails.add(it)
        } else if ("LOWGRD03".equals(it.fieldName, true)) {
            it.percentage = receivingData.DESMD
            filteredPriceDetails.add(it)
        } else if (gradeMappingDescription.toString().equals(it.description)) {
            it.percentage = receivingData.exportablePercentage
            filteredPriceDetails.add(it)
        }
    }
    return filteredPriceDetails
}

