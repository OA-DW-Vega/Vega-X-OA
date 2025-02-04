package com.olam.warehouse.vegax.qualitysesame.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityParamPost
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityPost
import com.olam.warehouse.vegax.qualitysesame.data.repo.VegaNigeriaSesameQualityRepository

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaNigeriaSesameQualityUseCase(private val repository: VegaNigeriaSesameQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)
    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()

    suspend fun postQuality(qualityPost: VegaNigeriaSesameQualityPost) = repository.postQuality(qualityPost)
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> =
        repository.getWBWithQuality(weighBridgeID)

    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()


    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        repository.updateWBDB(wbid, charg, message, status)

    // == Start MTNR ==

//    suspend fun getWeighBridgeDetailOnline() = repo.getWeighBridgeDetailOnline()
    suspend fun getLotDetailOnline(weighBridgeId: String) = repository.getLotDetailOnline(weighBridgeId)
//    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
//        repo.getQualityParams(materialId, valueExist, wbId)
    suspend fun postQualityMtnr(paramPost: VegaNigeriaSesameQualityParamPost) = repository.postQuality(paramPost)
//    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
//        repo.saveQualityData(qualityParameter, batchNo)
//    suspend fun getQualityOfflineList() = repo.getQualityOfflineList()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)
//    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repo.saveWBDB(weighBridge)

    // == End MTNR ==
}
