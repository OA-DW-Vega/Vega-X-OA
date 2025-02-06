package com.olam.warehouse.vegax.reconcilnicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnDetailsResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaInvoiceDao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentDate
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.reconcilnicaragua.data.api.VegaNicaraguaReconcilReportApi
import java.util.*


interface VegaNicaraguaReConcilReportRepository {
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getPriceConfigInfo(code: String): LiveData<VegaNicaraguaPriceConfigDetails>
    suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>>
    suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate>


    suspend fun getReceivingWithLineItem(startTime: Long, endTime: Long): LiveData<List<VegaReceiving>>
    suspend fun getInvoiceOfflineData(startTime: Long, endTime: Long): LiveData<List<VegaNicaraguaInvoiceDetails>>
    suspend fun getTransactionAdvanceData(startTime: Long, endTime: Long): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>>
    suspend fun updateReconReport(item: VegaNicaraguaReconcilCashMovement)
    suspend fun getReconReport(): LiveData<VegaNicaraguaReconcilCashMovement>
    suspend fun getQualityGradesListWithDesc(): LiveData<List<QualitativeParams>>
}

class VegaNicaraguaReConcilReportRepositoryImpl(
    private val api: VegaNicaraguaReconcilReportApi,
    private val dao: VegaNicaraguaInvoiceDao
) : VegaNicaraguaReConcilReportRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getPriceConfigInfo(code: String) = dao.getPriceConfigInfo(code,"")
    override suspend fun getGrnDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<GrnDetailsResponse>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GrnDetailsResponse>>>() {
            override suspend fun createCall() = api.getGrnDetails(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun getExchangeRateOffline() = dao.getExchangeRateOffline(currentKey)
    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceLineItemDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>>() {
            override suspend fun createCall() = api.getAdvanceLineItems(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun getReceivingWithLineItem(startTime: Long, endTime: Long) =
        dao.getReceivingWithLineItemByDate(startTime.toString(), endTime.toString())

    override suspend fun getInvoiceOfflineData(startTime: Long, endTime: Long) =
        dao.getInvoiceByDate(startTime.toString(), endTime.toString())

    override suspend fun updateReconReport(item: VegaNicaraguaReconcilCashMovement) = dao.insertReport(item)
    override suspend fun getReconReport(): LiveData<VegaNicaraguaReconcilCashMovement> =
        dao.getReconReport(getCurrentDate())

    override suspend fun getQualityGradesListWithDesc(): LiveData<List<QualitativeParams>> =
        dao.getQualityGradeDescList()

    override suspend fun getTransactionAdvanceData(startTime: Long, endTime: Long) =
        dao.getAdvanceByDate(startTime.toString(), endTime.toString())

}
