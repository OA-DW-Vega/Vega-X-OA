package com.olam.warehouse.vegax.qualityindiacoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPost
import com.olam.warehouse.vegax.qualityindiacoffee.data.repo.VegaIndiaCoffeeQualityRepository

class VegaIndiaCoffeeQualityUseCase(private val repository: VegaIndiaCoffeeQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun postQuality(qualityPost: VegaIndiaCoffeeQualityPost) = repository.postQuality(qualityPost)
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

    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighscale: Boolean) =
        repository.getWeighBridgeIdDetail(wbid, isWeighscale)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()

    suspend fun getPostProcessingQualityWBDetails() = repository.getPostProcessingQualityWBDetails()
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun getStocks(material: String) = repository.getStocks(material)

    suspend fun updateDB(wbid: String) = repository.updateDB(wbid)
}
