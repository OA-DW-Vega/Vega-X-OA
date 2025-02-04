package com.olam.warehouse.vegax.qualityecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.vegax.qualityecuador.data.repo.VegaEcuadorQualityRepository

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaEcuadorQualityUseCase(private val repository: VegaEcuadorQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)
    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()

    suspend fun postQuality(qualityPost: VegaQualityPost) = repository.postQuality(qualityPost)
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> =
        repository.getWBWithQuality(weighBridgeID)

    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        repository.updateWBDB(wbid, charg, message, status)

    suspend fun getSavedQuality(plantId: String, materialCode: String, batchNumber: String) =
        repository.fetchSavedWeighBridgeDetail(plantId, materialCode, batchNumber)
}
