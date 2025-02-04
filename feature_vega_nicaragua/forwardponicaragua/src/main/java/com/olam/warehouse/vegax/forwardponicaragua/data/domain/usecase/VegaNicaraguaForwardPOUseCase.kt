package com.olam.warehouse.vegax.forwardponicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.forwardponicaragua.data.repo.VegaNicaraguaInvoiceRepository

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaForwardPOUseCase(
    private val repository: VegaNicaraguaInvoiceRepository
) {
    suspend fun getSuppliers(purchaseOrgType: String?) = repository.getSuppliers(purchaseOrgType)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getGrades(materialCode: String) = repository.getGrades(materialCode)
    suspend fun getMaterialQualityGrades(materialCode: String) = repository.getMaterialQualityGrades(materialCode)
    suspend fun getGrnCharDetails(grade: String, materialCode: String): LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> = repository.getGrnCharDetails(grade, materialCode)
    suspend fun getGrnCharDetailsOffline(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>> = repository.getGrnCharDetailsOffline(grade, materialCode)

    suspend fun getGrnPriceDetailsOffline(): LiveData<List<VegaNicaraguaGrnPriceDetails>> =
        repository.getGrnPriceDetailsOffline()

    suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> =
        repository.getGrnPriceDetails()

    suspend fun getQualityParams(materialId: String) =
        repository.getQualityParams(materialId)

    suspend fun postForwardPO(receivingData: VegaNicaraguaForwardPoPost) = repository.postForwardPO(receivingData)
    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate> = repository.getExchangeRateOffline()
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> = repository.getExchangeRate()
    suspend fun saveForwardPOData(receivingData: VegaNicaraguaForwardPODetails) =
        repository.saveForwardPOData(receivingData)

    suspend fun saveForwardPOPriceDetails(receivingData: ArrayList<VegaNicaraguaForwardPOPriceDetails>) =
        repository.saveForwardPOPriceDetails(receivingData)

    suspend fun getForwardPODetails(): LiveData<List<VegaNicaraguaForwardPODetails>> = repository.getForwardPODetails()
    suspend fun getForwardPOPriceDetails(tempId: String): LiveData<List<VegaNicaraguaForwardPOPriceDetails>> =
        repository.getForwardPOPriceDetails(tempId)

    suspend fun deleteItem(tmpWbId: String) = repository.deleteItem(tmpWbId)
    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost) = repository.updateLotSequence(postData)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
}
