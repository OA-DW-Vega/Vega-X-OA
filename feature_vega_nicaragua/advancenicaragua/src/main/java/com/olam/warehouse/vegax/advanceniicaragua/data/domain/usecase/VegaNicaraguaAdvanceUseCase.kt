package com.olam.warehouse.vegax.advanceniicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails
import com.olam.warehouse.vegax.advanceniicaragua.data.repo.VegaNicaraguaAdvanceRepository

class VegaNicaraguaAdvanceUseCase(
    private val repository: VegaNicaraguaAdvanceRepository
) {
    suspend fun getSuppliers(purchaseOrgType: String) = repository.getSuppliers(purchaseOrgType)
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate> = repository.getExchangeRateOffline()
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> = repository.getExchangeRate()

    suspend fun getAdvanceDetailsByVendor(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>>> =
        repository.geAdvanceDetailsByVendor(vendorCode)

    suspend fun getAdvanceDetailsByVendorOffline(vendorCode: String): LiveData<VegaNicaraguaAdvanceDetails> =
        repository.geAdvanceDetailsByVendorOffline(vendorCode)

    suspend fun postAdvanceDetails(postingData: VegaNicaraguaAdvancePostRequest) = repository.postAdvanceDetails(postingData)
    suspend fun saveAdvanceTransactionDetails(postingData: VegaNicaraguaAdvanceTransactionDetails) =
        repository.saveAdvanceTransactionData(postingData)
    suspend fun deleteItem(tmpWbId: String) = repository.deleteItem(tmpWbId)
    suspend fun getAdvanceDetails(): LiveData<List<VegaNicaraguaAdvanceTransactionDetails>> = repository.getAdvanceDetails()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
}
