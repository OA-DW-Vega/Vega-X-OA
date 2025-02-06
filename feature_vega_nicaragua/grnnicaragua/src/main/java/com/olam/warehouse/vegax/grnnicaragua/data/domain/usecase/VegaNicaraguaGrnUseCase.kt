package com.olam.warehouse.vegax.grnnicaragua.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.*
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import com.olam.warehouse.vegax.grnnicaragua.data.repo.VegaNicaraguaGrnRepository
import java.util.*

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */
class VegaNicaraguaGrnUseCase(
    private val repository: VegaNicaraguaGrnRepository
) {
    suspend fun getSuppliers(purchaseOrgType: String?) = repository.getSuppliers(purchaseOrgType)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getStorageLocations() = repository.getStorageLocations()
    suspend fun getGrades(materialCode: String) = repository.getGrades(materialCode)
    suspend fun getMaterialQualityGrades(materialCode: String) = repository.getMaterialQualityGrades(materialCode)
    suspend fun saveBagDetails(material: VegaNicaraguaWeighmentBagMaterial) = repository.saveBagDetails(material)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String) = repository.deleteBagDetails(id, tmpWbId)
    suspend fun getBagItems(tmpWbId: String) = repository.getBagItems(tmpWbId)
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getPriceConfigInfo(materialCode: String, qualityCode: String) =
        repository.getPriceConfigInfo(materialCode, qualityCode)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun getExchangeRate(): LiveData<Resource<GenericReqAndResp<ExchangeRate>>> = repository.getExchangeRate()
    suspend fun getGrnPriceDetails(): LiveData<Resource<GenericReqAndResp<List<GrnPriceDetails>>>> =
        repository.getGrnPriceDetails()

    suspend fun getExchangeRateOffline(): LiveData<VegaNicaraguaExchangeRate> = repository.getExchangeRateOffline()
    suspend fun getGrnPriceDetailsOffline(): LiveData<List<VegaNicaraguaGrnPriceDetails>> =
        repository.getGrnPriceDetailsOffline()

    suspend fun postGrnDetails(receivingData: VegaNicaraguaGrnPost) = repository.postGrnDetails(receivingData)
    suspend fun getGrnCharDetails(grade: String, materialCode: String): LiveData<Resource<GenericReqAndResp<List<GrnCharDetails>>>> = repository.getGrnCharDetails(grade, materialCode)
    suspend fun getGrnCharDetailsOffline(
        grade: String,
        materialCode: String
    ): LiveData<List<VegaNicaraguaGrnCharDetails>> = repository.getGrnCharDetailsOffline(grade, materialCode)
    suspend fun getGradeMapping(grade: String): LiveData<VegaNicaraguaPositionGradeMappings> = repository.getGradeMapping(grade)
    suspend fun saveQualityData(prepareQualityData: VegaQuality) = repository.saveQualityData(prepareQualityData)
    suspend fun saveGrnData(receivingData: VegaReceiving) = repository.saveGrnData(receivingData)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceiving>>  = repository.getReceivingWithLineItem()
    suspend fun getQuality(tmpWbId: String): LiveData<List<VegaQuality>>  = repository.getQuality(tmpWbId)
    suspend fun updateGrnDataSuccess(
        data: VegaNicaraguaGrnPost,
        vegaReceiving: VegaReceiving
    ) = repository.updateGrnDataSuccess(data, vegaReceiving)

    suspend fun deleteAllItem(tmpWbId: String) = repository.deleteAllItem(tmpWbId)
    suspend fun getGlDetails(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getGlDetails(role)

    suspend fun getAdvanceLineDetails(vendorCode: String): LiveData<Resource<GenericReqAndResp<List<AdvanceLineItems>>>> =
        repository.geAdvanceLineItemDetails(vendorCode)

    suspend fun getAdvanceLineDetailsOffline(vendorCode: String): LiveData<List<VegaNicaraguaAdvanceLineItems>> =
        repository.geAdvanceLineItemDetailsOffline(vendorCode)

    suspend fun saveAdvanceLineItem(advanceLineItem: ArrayList<VegaNicaraguaAdvanceLineItemGrn>) =
        repository.saveAdvanceLineItem(advanceLineItem)

    suspend fun getAdvanceItem(tmpId: String) = repository.getAdvanceItem(tmpId)
    suspend fun updateLotSequence(postData: VegaNicaraguaUpdateLotSequencePost) = repository.updateLotSequence(postData)
    suspend fun updateSyncStartedStatus(tmpWbId: String) = repository.updateSyncStartedStatus(tmpWbId)
    suspend fun getPOList() = repository.getPOList()
    suspend fun getPOListLocal() = repository.getPOListLocal()
    suspend fun getGrnPrintDetails() = repository.getGrnPrintDetails()
    suspend fun getGrnPrintDetailsOffline() = repository.getGrnPrintDetailsOffline()
    suspend fun removeAdvanceLineItem(documentNumber: String?, tmpWbId: String) =
        repository.removeAdvanceLineItem(documentNumber, tmpWbId)

    suspend  fun updatePostingDate(tmpWbId: String, postdate: String) = repository.updatePostingDate(tmpWbId,postdate)
    suspend fun getReceivingWithLineItemDirect(): List<VegaReceiving>  = repository.getReceivingWithLineItemDirect()
    suspend fun getInvoiceOfflineData()  = repository.getInvoiceOfflineData()

    suspend fun getFarmerList() = repository.getFarmerList()

}
