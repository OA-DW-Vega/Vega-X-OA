package com.olam.warehouse.vegax.grnecuador.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.view.View
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.invoicenicaragua.R
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

/**
 * Created by Pavani  on 8/3/2020.
 */

const val INVOICE_SELECT_GRN_FRAG = "invoice_select_grn_fragment"
const val INVOICE_SELECT_VENDOR_FRAG = "invoice_select_vendor_fragment"
const val INVOICE_PRICE_CALCULATION_FRAG = "invoice_price_calculation_fragment"
const val FRAG_SUMMARY = "frag_summary"

const val A4_WIDTH = 3508
const val A4_HEIGHT= 2480

fun getTmpId() = Random.nextInt().toString()


fun bitmapToString(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    return  Base64.encodeToString(b, Base64.DEFAULT)
}
 fun getBitmapFromView(view: View, defaultColor: Int): Bitmap {
     view.measure(A4_WIDTH, A4_HEIGHT)
     var bitmap =
         Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
     var canvas = Canvas(bitmap)
     canvas.drawColor(defaultColor)
     view.layout(view.left, view.top, view.right, view.bottom)
     view.draw(canvas)
     return bitmap
 }


fun covertToDouble(value: String?): Double {
    if (value != null && value != "null" && value.length > 0) {
        val str = value.format(Locale.ENGLISH).replace(",", ".")
        return str.toDouble()
    }
    return 0.00
}

fun PrepareInvoiceData(
    vendorData: VegaVendor,
    grnData: GrnDetails,
    basePrice: String,
    preparingInvoiceData: ArrayList<com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoiceDetails>,
    totalPrice: String
): VegaNicaraguaInvoiceDetails {
    val data = VegaNicaraguaInvoiceDetails()
    data.tempId =
        if (grnData.tempId?.isEmpty() == true) "TMP".plus(Random.nextInt().toString()) else grnData.tempId.toString()
    data.item = grnData.item
    data.charg = grnData.batchNumber
    data.materialName = grnData.materialName
    data.materialNumber = grnData.materialNumber
    data.supplierName = grnData.supplierName
    data.supplierCode = grnData.supplierCode
    data.wbid = grnData.wbid
    data.werks = grnData.werks
    data.grn = grnData.grnNumber
    data.discount = grnData.discount
    data.pchar = grnData.pchar
    data.unitPrice = grnData.unitPrice
    data.basePrice = basePrice
    data.kpein = grnData.kpein
    data.totalPrice = totalPrice
    data.grnQty = grnData.toatlGrnQty
    data.grnType = grnData.grnType
    data.discountWeight = grnData.discountWeight
    data.meins = grnData.meins
    data.waers = grnData.waers
    data.qchar = grnData.qchar
    data.erdat = getCurrentTimeInMills().toString()
    data.werksName = grnData.werksName
    data.plantDesc = grnData.plantDesc
    data.bprme = grnData.bprme
    data.matkl = grnData.matkl
    data.netPayment = grnData.netPayment
    data.advance = grnData.advance
    data.qualityGrade = grnData.qualityGrade
    data.taxId = vendorData.taxNumber
    if (preparingInvoiceData.size > 0) {
        data.totalFTDC = preparingInvoiceData[0].ftdc
        data.basePrice = preparingInvoiceData[0].poPrice
        data.certificatePremium = preparingInvoiceData[0].certificatePremium
        data.volumePremium = preparingInvoiceData[0].volumePremium
        data.humidityPremium = preparingInvoiceData[0].humidityPremium
        data.qualityDiscounting = preparingInvoiceData[0].qualityDiscounting
        data.humidityDiscounting = preparingInvoiceData[0].humidityDiscounting
        data.grossValue = preparingInvoiceData[0].grossValue
        data.exportnCentives = preparingInvoiceData[0].exportIncentive
        data.withholdingTax = preparingInvoiceData[0].withHoldingTax
        data.NSExchangeRate = preparingInvoiceData[0].nationalStockExchange
        data.bankCommission = preparingInvoiceData[0].bankCommission
        data.totalDduction = preparingInvoiceData[0].subTotalDeductions
        data.finalPayment = preparingInvoiceData[0].finalPayment
        data.currency = preparingInvoiceData[0].currency
        data.qualityGradeDesc = preparingInvoiceData[0].qualityGradeDesc
        data.advanceSummary = preparingInvoiceData[0].advanceSummary
        data.advanceInterestSummary = preparingInvoiceData[0].advanceInterestSummary
        data.advanceCommissionSummary = preparingInvoiceData[0].advanceCommissionSummary
        data.advanceLegalExpenseSummary = preparingInvoiceData[0].advanceLegalExpenseSummary
        data.advanceMaintainceSummary = preparingInvoiceData[0].advanceMaintainceSummary
        data.totalAdvanceSummary = preparingInvoiceData[0].totalAdvanceSummary
        data.exchangeRate = preparingInvoiceData[0].exchangeRate
        data.netWeight = preparingInvoiceData[0].netWeight
        data.qualityGrade = preparingInvoiceData[0].grade
        data.certification = preparingInvoiceData[0].certification
    }
    return data
}

fun PrepareInvoiceDataReverse(grnData: VegaNicaraguaInvoiceDetails): GrnDetails {
    val data = GrnDetails()
    data.tempId = grnData.tempId
    data.item = grnData.item
    data.charg = grnData.charg
    data.materialName = grnData.materialName
    data.materialNumber = grnData.materialNumber
    data.supplierName = grnData.supplierName
    data.supplierCode = grnData.supplierCode.toString()
    data.wbid = grnData.wbid
    data.werks = grnData.werks
    data.grn = grnData.grn
    data.discount = grnData.discount
    data.pchar = grnData.pchar
    data.unitPrice = grnData.unitPrice
    data.basePrice = grnData.basePrice
    data.kpein = grnData.kpein
    data.totalPrice = grnData.totalPrice
    data.grnQty = grnData.grnQty
    data.grnType = grnData.grnType
    data.discountWeight = grnData.discountWeight
    data.meins = grnData.meins
    data.waers = grnData.waers
    data.qchar = grnData.qchar
    data.werksName = grnData.werksName
    data.plantDesc = grnData.plantDesc
    data.bprme = grnData.bprme
    data.matkl = grnData.matkl
    data.qualityGrade = grnData.qualityGrade
    return data
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


fun getTaxIdFromVendorList(
    supplierList: java.util.ArrayList<VegaVendor>,
    vendorCode: String?
): String? {
    val vendor = supplierList.filter { it.vendorCode.equals(vendorCode) }
    return if (vendor.size > 0) vendor[0].taxNumber else ""
}

private fun removeDollerInValue(value: String): String {

    var text = value.replace(App.getAppContext().getString(R.string.c_doller), "").trim()

    return text
}
