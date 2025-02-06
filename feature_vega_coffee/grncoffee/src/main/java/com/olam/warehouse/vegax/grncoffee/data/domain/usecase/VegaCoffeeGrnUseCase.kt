package com.olam.warehouse.vegax.grncoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnPost
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grncoffee.data.repo.VegaCoffeeGrnRepository

class VegaCoffeeGrnUseCase(
    private val repository: VegaCoffeeGrnRepository
) {
    suspend fun fetchWeighBridgeList() = repository.getWeighBridgeList()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)

    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)

    suspend fun postGrn(grnPost: VegaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>> = repository.getWeighBridgeDetail()
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = repository.updateGRNPrice(wbDetails)
    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) =
        repository.updateGrnSuccess(wbid, grnNo, batch, msg, status)

    suspend fun getOfflineWeighBridgeDetail() = repository.getOfflineWeighBridgeDetail()
    suspend fun getOfflineWeighBridgeDetailCount() = repository.getOfflineWeighBridgeDetailCount()
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        repository.updateGrnNoToQuality(wbid, grnNo, batchNo)
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getPOList() = repository.getPOList()
    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) =
            repository.getWeighBridgeIdDetail(wbid, isWeighScale)
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
            repository.getQualityParams(materialId, valueExist, wbId)
}
