package com.olam.warehouse.vegax.invoicenicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.modal.InventoryResponse
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.VendorGrnDetailsResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import com.olam.warehouse.vegax.invoicenicaragua.data.repo.VegaNicaraguaInvoiceRepository
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaInvoiceUseCase(
    private val repository: VegaNicaraguaInvoiceRepository
) {
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String) =
        repository.getPriceConfigInfo(materialCode, qualityCode)

    suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>> =
        repository.getGrnDetails(vendorCode)

    suspend fun getGrnInventoryDetails(lotId: String,materialCode:String): LiveData<Resource<GenericReqAndResp<InventoryResponse>>> =
        repository.getGrnInventoryDetails(lotId,materialCode)

    suspend fun getGrnInventoryDetailsOffline(lotId: String,materialCode:String): LiveData<List<VegaNicaraguaGRNInventoryDetails>> =
        repository.getGrnInventoryDetailsOffline(lotId,materialCode)

    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate> = repository.getExchangeRateOffline()
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> = repository.getExchangeRate()
    suspend fun getAdvanceLineDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> =
        repository.geAdvanceLineItemDetails(vendorCode)

    suspend fun getAdvanceLineDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaAdvanceLineItems>> =
        repository.geAdvanceLineItemDetailsOffline(vendorCode)

    suspend fun getGrnDetailsOffline(vendorCode: String) = repository.getGrnDetailsOffline(vendorCode)
    suspend fun saveInvoiceDetails(invoice: VegaNicaraguaInvoiceDetails) = repository.saveInvoiceDetails(invoice)
    suspend fun getInvoiceOfflineData() = repository.getInvoiceOfflineData()
    suspend fun deleteInvoiceItem(tmpWbId: String) = repository.deleteInvoiceItem(tmpWbId)
    suspend fun getAdvanceItem(tmpId: String) = repository.getAdvanceItem(tmpId)
    suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) =
        repository.saveAdvanceLineItem(advanceLineItem)

    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>> = repository.getReceivingWithLineItem()

    suspend fun getQualityDesc(grade: String) = repository.getQualityDesc(grade)

    suspend fun getQualityGradesListWithDesc() = repository.getQualityGradesListWithDesc()
    suspend fun postInvoiceDetails(receivingData: VegaNicaraguaInvoicePostRequest) =
        repository.postInvoiceDetails(receivingData)

    suspend fun getInvoiceReceipt() = repository.getInvoiceReceipt()
    suspend fun getInvoiceReceiptOffline() = repository.getInvoiceReceiptOffline()
    suspend fun removeAdvanceLineItem(documentNumber: String?, tempId: String) =
        repository.removeAdvanceLineItem(documentNumber, tempId)
}
