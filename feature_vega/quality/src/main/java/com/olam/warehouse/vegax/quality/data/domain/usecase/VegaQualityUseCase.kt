package com.olam.warehouse.vegax.quality.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPost
import com.olam.warehouse.vegax.quality.data.repo.VegaQualityRepository

class VegaQualityUseCase(private val repository: VegaQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)
    suspend fun postQuality(qualityPost: VegaQualityPost) = repository.postQuality(qualityPost)
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        repository.updateWBDB(wbid, charg, message, status)
    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()
    suspend fun updateDeletedItem(wbid: String) = repository.updateDeletedItem(wbid)
    //suspend fun getOfflineParamItems(wbid: String) = repository.getOfflineParamItems(wbid)
    suspend fun getWBWithQuality(weighBridgeID: String) = repository.getWBWithQuality(weighBridgeID)

    suspend fun updateTempIdToWbid(wbid: String, tempId: String) = repository.updateTempIdToWbid(wbid, tempId)
    suspend fun updateWBMessage(msg: String, weighBridgeId: String?) = repository.updateWBMessage(msg, weighBridgeId)
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getPostProcessingQualityWBDetails() = repository.getPostProcessingQualityWBDetails()
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
}
