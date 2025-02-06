package com.olam.warehouse.vegax.invoicenicaragua.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaInvoiceDao
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaPriceConfigDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.INVOICE_DATA
import com.olam.warehouse.presentation.utils.UIUtils.INVOICE_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.grnecuador.utils.covertToDouble
import com.olam.warehouse.vegax.invoicenicaragua.data.api.VegaNicaraguaInvoiceApi
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaNicaraguaInvoicePostWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: VegaNicaraguaInvoiceApi by inject()
        val dao: VegaNicaraguaInvoiceDao by inject()
        val wbid = inputData.getString(INVOICE_DATA) ?: ""
        val invoiceData=dao.getInvoiceData(wbid)
        val exchangeData = dao.getExchangeRateOfflineWorker(getCurrentKey())
        val exchangeRate = if (exchangeData != null) exchangeData.exchangeRate else ""
        val usdAmount = if (exchangeData != null) exchangeData.currencyValue else ""
        val currency = if (exchangeData != null) exchangeData.currencyCode?.toString() else ""
        val priceConfig = dao.getPriceConfigInfoWorker(
            invoiceData.materialNumber.toString(),
            invoiceData.qualityGrade!!.substring(invoiceData.qualityGrade!!.length - 4)
        )
        val advanceValues = dao.getAdvanceItemById(wbid)

        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

        val post = VegaNicaraguaInvoicePostRequest()

        post.invoiceDetails =
            preparingInvoiceData(advanceValues, priceConfig, invoiceData, exchangeRate!!, usdAmount!!, wbid, currency)
        post.key = currentKey

        try {

            val response = api.syncInvoiceData(post).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    if (resp?.data?.invoiceFlag == true) {
                        invoiceData.syncStatusMsg =
                            if (resp.message.isNotEmpty()) resp.message else resp.data.message
                        invoiceData.isSynced = true
                    }
                    else {
                        invoiceData.syncStatusMsg =
                            if (respData.errorMessage?.isNotEmpty() == true) respData.errorMessage else respData.message
                        invoiceData.isSynced = false
                    }
                    dao.saveInvoiceData(invoiceData)
                    Result.success(workDataOf(INVOICE_OUTPUT_DATA to resp?.data?.wbId))

                } else {
                    val msg = resp?.message ?: resp?.errors
                    dao.updateErrorInvoiceData(wbid, msg.toString())
                    Result.failure(workDataOf(INVOICE_OUTPUT_DATA to msg))
                }
            } else {
                val msg = response.message()
                dao.updateErrorInvoiceData(wbid, msg.toString())
                Result.failure(workDataOf(INVOICE_OUTPUT_DATA to response.message()))

            }

        } catch (error: Throwable) {

            Result.failure(workDataOf(INVOICE_OUTPUT_DATA to error.message))

        }


//        }
    }
}

private fun preparingInvoiceData(
    advanceValues: List<VegaNicaraguaAdvanceLineItemGrn>,
    priceConfigDetails: VegaNicaraguaPriceConfigDetails,
    receivingData: VegaNicaraguaInvoiceDetails,
    exchangeRate: String,
    USDAmount: String,
    wbid: String,
    currency: String?
): java.util.ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceDetails> {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
   /* var certificatePremium: Double? = 0.0
    var volumePremium: Double? = 0.0
    var humidityPremium: Double? = 0.0
    var qualityDiscount: Double? = 0.0
    var totalPrice: Double? = 0.0
    var grossValue: Double = 0.0
    var netPayment: Double? = 0.0
    var ftdc: Double = 0.0
    var netWeight = receivingData.grnQty*/
    var invoiceList = java.util.ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceDetails>()
    if (priceConfigDetails != null) {
        try {
            val grnItemJson = PreferenceHelper.get(wbid, "")
            val grnList = Gson().fromJson<List<GrnDetails>>(grnItemJson)
            var advance = 0.0
            if (advanceValues.size > 0) {
                advanceValues.forEach {
                    advance = covertToDouble(it.totalAdvanceKnockAmount!!) + advance
                }
            }

           /* totalPrice = covertToDouble(priceConfigDetails.dailyPrice!!) * netWeight!!.toDouble()
            certificatePremium = covertToDouble(priceConfigDetails.certificatePremium) * netWeight.toDouble()
            var volumeTo = covertToDouble(priceConfigDetails.volumePremiumTo)
            var volumeFrom = covertToDouble(priceConfigDetails.volumePremiumFrom)

            grnList.forEach {
                val netWeightForEach = it.grnQty

                ftdc = ftdc.plus(
                    ((covertToDouble(USDAmount)) * (covertToDouble(exchangeRate)) * (covertToDouble(it.inventoryDetail?.exportable) / 100) * netWeightForEach!!.toDouble()) / 46
                )

                if (volumeFrom > 0 && volumeTo > 0) {
                    if (netWeightForEach.toDouble() >= volumeFrom && netWeightForEach.toDouble() <= volumeTo) {
                        volumePremium =
                            volumePremium?.plus((covertToDouble(priceConfigDetails.volumePremium) * netWeightForEach.toDouble()))
                    }
                } else if (volumeFrom > 0) {
                    if (netWeightForEach.toDouble() >= volumeFrom) {
                        volumePremium =
                            volumePremium?.plus(covertToDouble(priceConfigDetails.volumePremium) * netWeightForEach.toDouble())
                    }
                } else if (volumeTo > 0) {
                    if (netWeightForEach.toDouble() <= volumeTo) {
                        volumePremium =
                            volumePremium?.plus(covertToDouble(priceConfigDetails.volumePremium) * netWeightForEach.toDouble())
                    }
                } else {
                    volumePremium = 0.00
                }
            }
            humidityPremium = covertToDouble(priceConfigDetails.moisturePremium) * netWeight.toDouble()
            qualityDiscount = covertToDouble(priceConfigDetails.qualityDiscount) * netWeight.toDouble()
            val humidityDiscount: Double =
                (totalPrice + certificatePremium + volumePremium!! + humidityPremium) * (covertToDouble(
                    priceConfigDetails.moistureDiscount
                ) / 100)
            grossValue =
                (totalPrice + certificatePremium + volumePremium!! + humidityPremium) - (qualityDiscount + humidityDiscount)
            val exportIncentive: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.exportIncentive) / 100)
            val withHoldingTax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.withHoldingTax) / 100)
            val NSETax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.neCommission) / 100)
            val commissionTax: Double =
                (grossValue) * (covertToDouble(priceConfigDetails.bankCommsion) / 100)
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

            val info = com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceDetails()
            /*info.advanceKnockOffAmt = String.format(Locale.ENGLISH, "%.2f", advanceSummary)
            info.advanceLineItems = prepareAdvanceLineItems(advanceValues)
            info.poPrice = receivingData.basePrice
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
            info.grnNetWeight = netWeight
            info.exchangeRate = exchangeRate
            info.grade = receivingData.qualityGrade
            info.vendorCode = receivingData.supplierCode
            info.vendorName = receivingData.supplierName
            info.tempNumber = receivingData.tempId
            info.plant = getPlantDetails()
            info.netWeight = netWeight
            info.materialCode = receivingData.materialNumber
            info.materialName = receivingData.materialName
            info.currency = currency
            info.uom = receivingData.charg
            info.grnType = "ptbf"
            info.invoiceDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
            var certi = ""
            if (grnList.size > 0) {
                if (grnList[0].inventoryRes?.inventory?.size ?: 0 > 0)
                    certi = grnList[0].inventoryRes?.inventory?.get(0)?.certification.toString()
            }
            info.certification = certi
            info.bagCount = receivingData.bagsCount
            info.grossWeight = receivingData.grossWeight
            info.tareWeight = receivingData.tareWeight
            info.batchNumber = receivingData.charg
            info.qualityDiscounting = qualityDiscount.toString()
            info.grnList = grnList*/

            info.advanceKnockOffAmt = receivingData.advanceSummary
            info.advanceSummary = receivingData.advanceSummary.toString()
            info.advanceInterestSummary = receivingData.advanceInterestSummary
            info.advanceCommissionSummary = receivingData.advanceCommissionSummary
            info.advanceLegalExpenseSummary = receivingData.advanceLegalExpenseSummary
            info.totalAdvanceSummary = receivingData.totalAdvanceSummary
            info.advanceMaintainceSummary = receivingData.advanceMaintainceSummary
            info.advanceLineItems = prepareAdvanceLineItems(advanceValues)
            info.bankCommission = receivingData.bankCommission
            info.certificatePremium = receivingData.certificatePremium
            info.exchangeRate = receivingData.exchangeRate
            info.exportIncentive = receivingData.exportnCentives
            info.finalPayment = receivingData.finalPayment
            info.ftdc = receivingData.totalFTDC
            info.grnNetWeight = receivingData.netWeight
            info.grnType = "ptbf"
            info.grossValue = receivingData.grossValue
            info.humidityPremium=receivingData.humidityPremium
            info.humidityDiscounting = receivingData.humidityDiscounting
            info.invoiceDate = receivingData.erdat?.toLong()?.let { DateUtils.getDate(it, "yyyyMMdd") }
            info.key = currentKey
            info.materialCode = receivingData.materialNumber
            info.materialName = receivingData.materialName
            info.nationalStockExchange = receivingData.NSExchangeRate
            info.netPayment = receivingData.netPayment
            info.netWeight = receivingData.netWeight
            info.plant = getPlantDetails()
            info.poPrice = receivingData.basePrice
            if (grnList.size > 0) {
                if (grnList[0].inventoryRes?.inventory?.size ?: 0 > 0) {
                    info.grade = grnList[0].inventoryRes?.inventory?.get(0)?.gradeDesc
                    info.qualityGradeDesc = grnList[0].inventoryRes?.inventory?.get(0)?.gradeDesc
                    info.certification = grnList[0].inventoryRes?.inventory?.get(0)?.certification
                } else {
                    info.grade = receivingData.qualityGrade
                    info.qualityGradeDesc = receivingData.qualityGradeDesc
                    info.certification = receivingData.certification
                }
            }
            info.qualityPremium = receivingData.qualityDiscounting
            // info.storageLocationCode=grnData.
            //info.storageLocationName=grnData.
            info.subTotalDeductions = receivingData.totalDduction
            info.tempNumber = receivingData.invoiceNo
            info.totalPrice = receivingData.totalPrice
            info.vendorCode = receivingData.supplierCode
            info.vendorName = receivingData.supplierName
            info.volumePremium = receivingData.volumePremium
            info.withHoldingTax = receivingData.withholdingTax
            info.bagCount = receivingData.bagsCount
            info.grossWeight = receivingData.grossWeight
            info.tareWeight = receivingData.tareWeight
            info.batchNumber = receivingData.charg
            info.uom = receivingData.meins
            info.currency = currency
            info.qualityDiscounting = receivingData.qualityDiscounting
            info.grnList = grnList
            invoiceList.add(info)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    return invoiceList
}

private fun prepareAdvanceLineItems(advanceKnockOfList: List<VegaNicaraguaAdvanceLineItemGrn>): ArrayList<VegaNicaraguaAdvanceLineItems> {
    var advanceLineItems =
        ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems>()

    advanceKnockOfList.forEach {
        var advanceItem = com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaAdvanceLineItems()
        advanceItem.advanceAmount = it.amount
        advanceItem.itemNum = it.itemNum
        advanceItem.advanceDate = it.documentDate
        advanceItem.advanceDocumentNumber = it.documentNumber
        advanceItem.currencyDevaluation = it.currencyDevaluationAmount
        advanceItem.interestCommission = it.commissionAmount
        advanceItem.knockOffAmt = it.advanceKnockAmount
        advanceItem.grossValue = it.totalAdvanceKnockAmount
        advanceItem.interest = it.interestAmount
        advanceItem.legalExpense = it.legalExpenseAmount
        advanceItem.commissionAmount = it.commissionAmount
        advanceLineItems.add(advanceItem)
    }

    return advanceLineItems
}
