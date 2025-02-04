package com.olam.warehouse.master.veganicaragua.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper

/**
 * Created by Keerthi Santhanam on 9/04/2020.
 */

data class VegaNicaraguaGrnPost(
    var batchNumber: String? = "",
    var certificate: String? = "",
    var errorMessage: String? = "",
    var exchangeRate: String? = "",
    var grade: String? = "",
    var grnData: List<VegaNicaraguaGrnData>? = null,
    var grnFlag: Boolean? = false,
    var grnNumber: String? = "",
    var poNumber: String? = "",
    var grnType: String? = "",
    var transactionMode: String? = "",
    var key: String? = "",
    var lotDetails: List<VegaNicaraguaLotDetails>? = null,
    var materialBasePrice: String? = "",
    var materialBaseUnit: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var materialPrice: String? = "",
    var plant: Plant? = null,
    var priceDetails: List<VegaNicaraguaGrnPriceDetails>? = null,
    var purchaseDocType: String? = "",
    var purchaseGrp: String? = "",
    var purchaseOrg: String? = "",
    var qcFlag: Boolean? = false,
    var tollingDetails: List<VegaNicaraguaTollingDetails>? = null,
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var wbFlag: Boolean? = false,
    var wbId: String? = "",
    var qualityGradeDesc: String? = "",
    var cascara: String? = "",
    var humedad: String? = "",
    var rendimientoBruto: String? = "",
    var invoiceDetails: List<VegaNicaraguaInvoiceDetails>? = null,
    var weighDetails: List<VegaNicaraguaWeighmentBagMaterial>? = null,
    var invoiceFlag: Boolean? = false,
    var tempGRNNumber: String? = "",
    var masterSyncAt: String? = PreferenceHelper.get(Constants.LAST_SYNC_TIME, ""),
    var loginAt: String? = PreferenceHelper.get(Constants.LOGIN_AT, "")
)

data class VegaNicInvoiceReceipt(
    var invoiceNumber: String? = "",
    var currency: String? = "",
    var invoiceDBDetails: VegaNicaraguaInvoiceDetails? = null
)

data class VegaNicaraguaInvoiceDetails(

    var advanceKnockOffAmt: String? = "",
    var advanceLineItems: List<VegaNicaraguaAdvanceLineItems>? = null,
    var bankCommission: String? = "",
    var certificatePremium: String? = "",
    var exchangeRate: String? = "",
    var exportIncentive: String? = "",
    var finalPayment: String? = "",
    var ftdc: String? = "",
    var grade: String? = "",
    var grnNetWeight: String? = "",
    var grnType: String? = "",
    var grnNumber: String? = "",
    var grossValue: String? = "",
    var humidityDiscounting: String? = "",
    var humidityPremium: String? = "",
    var qualityDiscounting: String? = "",
    var invoiceDate: String? = "",
    var key: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var nationalStockExchange: String? = "",
    var netPayment: String? = "",
    var netWeight: String? = "",
    var bagCount: String? = "",
    var grossWeight: String? = "",
    var tareWeight: String? = "",
    var certification: String? = "",
    var batchNumber: String? = "",
    var plant: Plant? = null,
    var poPrice: String? = "",
    var qualityGradeDesc: String? = "",
    var qualityPremium: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var subTotalDeductions: String? = "",
    var tempNumber: String? = "",
    var totalPrice: String? = "",
    var vendorCode: String? = "",
    var vendorName: String? = "",
    var volumePremium: String? = "",
    var withHoldingTax: String? = "",
    var poNumber: String? = "",
    var currency: String? = "",
    var uom: String? = ""
)

data class VegaNicaraguaAdvanceLineItems(

    var advanceAmount: String? = "",
    var advanceDate: String? = "",
    var itemNum: String? = "",
    var advanceDocumentNumber: String? = "",
    var currencyDevaluation: String? = "",
    var grossValue: String? = "",
    var interestCommission: String? = "",
    var interest: String? = "",
    var knockOffAmt: String? = "",
    var legalExpense: String? = ""
)


data class VegaNicaraguaGrnData(
    var batchNumber: String? = "",
    var currency: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var netWeight: String? = "",
    var createdDate: String? = "",
    var docDate: String? = "",
    var plant: String? = "",
    var price: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseOrderNum: String? = "",
    var storageLocationCode: String? = "",
    var supplierCode: String? = "",
    var unitsOfMeasure: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = ""
)

data class VegaNicaraguaLotDetails(

    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "",
    var batchNumber: String? = "",
    var currency: String? = "",
    var customerNum: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var finalApproval: String? = "",
    var grnNumber: String? = "",
    var grossWeight: String? = "",
    var item: String? = "",
    var kor: String? = "",
    var materialCode: String? = "",
    var netWeight: String? = "",
    var paidWeight: String? = "",
    var plant: String? = "",
    var pmat2Count: String? = "",
    var pmat2Type: String? = "",
    var pmat2Weight: String? = "",
    var pmat3Count: String? = "",
    var pmat3Type: String? = "",
    var pmat3Weight: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var qualityDetails: List<VegaNicaraguaQualityDetails>?=null,
    var qualityFlag:String? = "",
    var referenceId: String? = "",
    var storageLocation: String? = "",
    var storageLocationCode: String? = "",
    var supplierCode: String? = "",
    var unitsOfMeasure: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = ""
)

data class VegaNicaraguaTollingDetails(

    var costCenter: String? = "",
    var distrPerc: String? = "",
    var glAccount: String? = "",
    var netValue: String? = "",
    var quantity: String? = ""
)

data class VegaNicaraguaQualityDetails(

    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)
