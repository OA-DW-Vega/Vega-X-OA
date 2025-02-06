package com.olam.warehouse.vegax.invoicenicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.InventoryResponse
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.VendorGrnDetailsResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaInvoiceDao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicGrnInvoiceWithAdvanceItems
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.master.veganicaragua.model.VegaNicPtbfInvoiceWithAdvanceItems
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnWithInventoryDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.invoicenicaragua.data.api.VegaNicaraguaInvoiceApi
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
interface VegaNicaraguaInvoiceRepository {
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String): LiveData<VegaNicaraguaPriceConfigDetails>
    suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>>
    suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate>
    suspend fun geAdvanceLineItemDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaAdvanceLineItems>>
    suspend fun getGrnDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaGrnWithInventoryDetails>>
    suspend fun saveInvoiceDetails(invoice: VegaNicaraguaInvoiceDetails)
    suspend fun getInvoiceOfflineData(): LiveData<List<VegaNicaraguaInvoiceDetails>>
    suspend fun deleteInvoiceItem(tmpWbId: String)
    suspend fun getAdvanceItem(tmpId: String): LiveData<List<VegaNicaraguaAdvanceLineItemGrn>>
    suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>)
    suspend fun getGrnInventoryDetails(
        lotId: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<InventoryResponse>>>

    suspend fun getGrnInventoryDetailsOffline(
        lotId: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGRNInventoryDetails>>

    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>>
    suspend fun getQualityDesc(grade: String): LiveData<QualitativeParams>

    suspend fun getQualityGradesListWithDesc(): LiveData<List<QualitativeParams>>
    suspend fun postInvoiceDetails(receivingData: VegaNicaraguaInvoicePostRequest): LiveData<Resource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>>
    suspend fun getInvoiceReceipt(): LiveData<Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>>
    suspend fun getInvoiceReceiptOffline(): LiveData<List<VegaNicaraguaInvoiceDetails>>
    suspend fun removeAdvanceLineItem(documentNumber: String?, tempId: String)
    suspend fun updatePostingDate(tmpWbId: String, postdate: String)
    suspend fun getReceivingWithAdvanceLineItem(): LiveData<List<VegaNicGrnInvoiceWithAdvanceItems>>
    suspend fun getInvoiceWithAdvanOfflineData(): LiveData<List<VegaNicPtbfInvoiceWithAdvanceItems>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
}

class VegaNicaraguaInvoiceRepositoryImpl(private val api: VegaNicaraguaInvoiceApi, private val dao: VegaNicaraguaInvoiceDao) : VegaNicaraguaInvoiceRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String) =
        dao.getPriceConfigInfo(materialCode, qualityCode)

    override suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VendorGrnDetailsResponse>>>() {
            override suspend fun createCall() = api.getGrnDetails(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun getExchangeRateOffline() = dao.getExchangeRateOffline(currentKey)
    override suspend fun getGrnDetailsOffline(vendorCode: String) = dao.getGrnDetailsOffline(vendorCode)
    override suspend fun saveInvoiceDetails(invoice: VegaNicaraguaInvoiceDetails) = dao.saveInvoiceDetails(invoice)
    override suspend fun getInvoiceOfflineData() = dao.getInvoiceOfflineData()
    override suspend fun deleteInvoiceItem(tmpWbId: String) = dao.deleteInvoiceItem(tmpWbId)

    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<AdvanceLineItems>>>() {
            override suspend fun createCall() = api.getAdvanceLineItems(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceLineItemDetailsOffline(vendorCode: String) = dao.getAdvanceLineItems(vendorCode)
    override suspend fun getAdvanceItem(tmpId: String) = dao.getAdvanceItem(tmpId)
    override suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) =
        dao.saveAdvanceLineItem(advanceLineItem)

    override suspend fun getGrnInventoryDetails(
        lotId: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<InventoryResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<InventoryResponse>>() {
            override suspend fun createCall() = api.getGrnInventoryDetails(currentKey, lotId,materialCode)
        }.build().asLiveData()
    }

    override suspend fun getGrnInventoryDetailsOffline(
        lotId: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGRNInventoryDetails>> {
        return dao.getInventoryDetails(lotId, materialCode)
    }

    override suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>> = dao.getReceivingWithLineItem()
    override suspend fun getQualityDesc(grade: String): LiveData<QualitativeParams> {
        return dao.getQualityDesc(grade)
    }

    override suspend fun getQualityGradesListWithDesc(): LiveData<List<QualitativeParams>> {
        return dao.getQualityGradeDescList()
    }

    override suspend fun postInvoiceDetails(receivingData: VegaNicaraguaInvoicePostRequest): LiveData<Resource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaInvoicePostRequest> =
                api.postInvoiceData(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getInvoiceReceipt(): LiveData<Resource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicInvoiceReceipt>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNicInvoiceReceipt>> =
                api.getInvoiceReceipt(false, getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getInvoiceReceiptOffline() = dao.getInvoiceReceiptOffline()
    override suspend fun removeAdvanceLineItem(documentNumber: String?, tempId: String) =
        dao.removeAdvanceLineItem(documentNumber, tempId)
    override suspend fun updatePostingDate(tmpWbId: String, postdate: String)  = dao.updatePostingDate(tmpWbId,postdate)
    override suspend fun getReceivingWithAdvanceLineItem() = dao.getReceivingWithAdvanceLineItem()
    override suspend fun getInvoiceWithAdvanOfflineData() = dao.getInvoiceWithAdvanOfflineData()
    override suspend fun getProducts(): LiveData<List<VegaMaterial>> = dao.getProducts()
}

