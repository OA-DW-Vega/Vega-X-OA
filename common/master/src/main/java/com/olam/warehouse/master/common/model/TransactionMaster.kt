package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.dorigin.entity.DOSapMaterialList
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.master.dorigin.entity.DispatchDetail
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.master.vega.model.VegaGhanaProcessOrder
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaQualityWBDetail
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaStorageLocation
import com.olam.warehouse.master.vegaghana.model.VegaGhanaLotQuality
import com.olam.warehouse.master.vegaghana.model.VegaGhanaMtnBatchNumber
import com.olam.warehouse.master.vegaghana.model.VegaGhanaRminBom
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrderModelCommon
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
data class TransactionMaster(
    var key: String? = "",
    var masterDTO: MasterDTO = MasterDTO()
)

data class MasterDTO(
    var id: Int = 0,
//    var saleDeliveryOrderList: List<DeliveryOrders>? = emptyList(),
//    var mtnDeliveryOrderList: List<DeliveryOrders>? = emptyList(),
    //var wsitemDetails: List<QualityWBDetails>? = emptyList(),
    var wbListDetailsDTO: List<WeighBridge>? = emptyList(),
//    var doTransList: List<DOTransactionDetail>? = emptyList(),
    var doTransList: List<DOTxnDetail>? = emptyList(),
    var doDispatchDetails: List<DispatchDetail>? = emptyList(),
    var purchaseOrderList: List<PurchaseOrder>? = emptyList(),
    var dispatchPlantMTNTPurchaseOrderList: List<PurchaseOrder>? = emptyList(),
    var mtntPurchaseOrderList: List<VegaMtntPurchaseOrder>? = emptyList(),
    var stockDetails: List<Stocks>? = emptyList(),
    var grnPriceDetails: List<GrnPriceDetails>? = emptyList(),
    var vendorGRNDetails: List<VendorGrnDetails>? = emptyList(),
    var mtnDetails: VegaReceivingMtnWrapper? = null,
    var vegaProcessOrderTransDTO: VegaGhanaProcessOrder? = null,
    var mtnBatchQualityDTO: List<VegaGhanaMtnBatchNumber>? = null,
    var lotQualityDetails: List<VegaGhanaLotQuality>? = null,
    var vegaFetchBomResponseList: List<VegaGhanaRminBom>? = null,
    var exchangeRate: ExchangeRate? = null,
    var grnCharDetails: List<GrnCharDetails>? = emptyList(),
    var advanceLineItems: List<AdvanceLineItems>? = emptyList(),
    var offlineInventory: OfflineInventory? = null,
    var wbListHeaderDetailsDTO: List<VegaCoCoaQualityWBDetail>? = emptyList(),
    var storageLocationLst: List<VegaCoCoaStorageLocation>? = emptyList(),
    var advanceDetails: List<VegaNicaraguaGetAdvanceDetails>? = emptyList(),
    var invoiceDetails: List<VegaNicInvoiceReceipt>? = emptyList(),
    var salesOrderDetails: List<VegaIndoCoffeeExportSalesOrderModelCommon>? = emptyList(),
    var receiptReprintDto: List<VegaReceiving>? = emptyList(),
    var sapMatProductDTO: List<DOSapMaterialList>?= emptyList(),
    var sourceLotDTO: List<TrackTraceSourceLotDetails>? = emptyList(),
    var vendorTransactionDTO: List<TrackTraceModelTransactionIdDetails>? = emptyList()



    )

data class TransCountModel(
    var poCount:Int?= 0,
    var invoiceCount:Int?= 0,
    var grnReprintCount:Int?= 0,
    var grnPriceCount:Int?= 0,
    var charDetailsCount:Int?= 0,
    var advanceCount:Int?= 0,
    var vendorCreditCount:Int?= 0,
    var stocksCount:Int?= 0
)
