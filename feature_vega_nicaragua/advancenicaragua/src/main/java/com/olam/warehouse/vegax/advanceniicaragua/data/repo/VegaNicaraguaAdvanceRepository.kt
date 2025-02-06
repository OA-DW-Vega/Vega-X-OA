package com.olam.warehouse.vegax.advanceniicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaAdvanceDao
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.advanceniicaragua.data.api.VegaNicaraguaAdvanceApi
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostResponse
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails
import java.util.*


interface VegaNicaraguaAdvanceRepository {
    suspend fun getSuppliers(purchaseOrgType: String): LiveData<List<VegaVendor>>
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>>
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate>
    suspend fun geAdvanceDetailsByVendor(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>>
    suspend fun geAdvanceDetailsByVendorOffline(vendorCode: String): LiveData<VegaNicaraguaAdvanceDetails>
    suspend fun postAdvanceDetails(postData: VegaNicaraguaAdvancePostRequest): LiveData<Resource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>>
    suspend fun deleteItem(tmpWbId: String)
    suspend fun saveAdvanceTransactionData(receivingData: VegaNicaraguaAdvanceTransactionDetails)
    suspend fun getAdvanceDetails(): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}

class VegaNicaraguaAdvanceRepositoryImpl(
    private val api: VegaNicaraguaAdvanceApi,
    private val dao: VegaNicaraguaAdvanceDao
) : VegaNicaraguaAdvanceRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")
    override suspend fun getSuppliers(purchaseOrgType: String) = dao.getSuppliers(purchaseOrgType)
    override suspend fun getExchangeRateOffline() = dao.getExchangeRateOffline(currentKey)
    override suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<ExchangeRate>>() {
            override suspend fun createCall() = api.getExchangeRate(currentDate.toString(), currentKey)
        }.build().asLiveData()
    }
    override suspend fun postAdvanceDetails(receivingData: VegaNicaraguaAdvancePostRequest): LiveData<Resource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicaraguaAdvancePostResponse> =
                api.postAdvanceData(receivingData)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceDetailsByVendor(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>() {
            override suspend fun createCall() = api.getAdvanceDetails(currentKey, vendorCode)
        }.build().asLiveData()
    }

    override suspend fun geAdvanceDetailsByVendorOffline(vendorCode: String) = dao.getAdvanceDetails(vendorCode)

    override suspend fun saveAdvanceTransactionData(receivingData: VegaNicaraguaAdvanceTransactionDetails) =
        dao.saveAdvanceDetailsData(receivingData)

    override suspend fun deleteItem(tmpWbId: String) {
        dao.deleteAdvanceDetails(tmpWbId)
    }

    override suspend fun getAdvanceDetails()=dao.geAdvanceDetailsData()

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
}
