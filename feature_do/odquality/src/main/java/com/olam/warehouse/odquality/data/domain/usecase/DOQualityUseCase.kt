package com.olam.warehouse.odquality.data.domain.usecase

import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.repo.DOQualityRepository

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOQualityUseCase(private val repository: DOQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getSAPMaterials() = repository.getSAPMaterials()

    suspend fun postQuality(qualityPost: DOQualityPost) = repository.postQuality(qualityPost)
    suspend fun updateWBDB(wbid: String) = repository.updateWBDB(wbid)
    suspend fun updateTempIdToWbid(wbid: String, grossWeight: String, tempId: String) =
        repository.updateTempIdToWbid(wbid, grossWeight, tempId)
    suspend fun getWBWithQuality(weighBridgeID: String) = repository.getWBWithQuality(weighBridgeID)
    suspend fun saveQualityData(qualityParameter: DOQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun updateDeletedItem(wbid: String) = repository.updateDeletedItem(wbid)
    suspend fun updateWBMessage(msg: String, weighBridgeId: String?) = repository.updateWBMessage(msg, weighBridgeId)
    suspend fun getSavedQuality(plantId: String, materialCode: String, batchNumber: String) =
        repository.fetchSavedWeighBridgeDetail(plantId, materialCode, batchNumber)

    suspend fun getQualityWeighBridgeBagDetail(key: String, plantId: String) = repository.getQualityWeighBridgeBagDetail(key, plantId)
}
