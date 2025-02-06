package com.olam.warehouse.vegax.qualitynigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.qualitynigeria.data.repo.VegaNigeriaQualityRepository

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaNigeriaQualityUseCase(private val repository: VegaNigeriaQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)

    suspend fun postQuality(qualityPost: VegaQualityPost) = repository.postQuality(qualityPost)
    suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaPost) =
        repository.postNigeriaQuality(qualityPost)

    suspend fun fetchQcWeighBridgeList(selectedPlantId: String) = repository.getqcWeighBridgeList(selectedPlantId)
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> =
        repository.getWBWithQuality(weighBridgeID)

    suspend fun updateDeletedItem(weighBridgeId: String) =
        repository.updateDeletedItem(weighBridgeId)

    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        repository.updateWBDB(wbid, charg, message, status)

    suspend fun getPortPlantsId(role: String) = repository.getPortPlantsId(role)

    suspend fun getVegaMaterials() = repository.getVegaMaterials()
}
