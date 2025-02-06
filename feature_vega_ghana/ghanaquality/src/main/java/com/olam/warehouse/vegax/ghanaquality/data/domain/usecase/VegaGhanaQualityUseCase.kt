package com.olam.warehouse.vegax.ghanaquality.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQuality
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityParamPost
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityPost
import com.olam.warehouse.vegax.ghanaquality.data.repo.VegaGhanaQualityRepository

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaGhanaQualityUseCase(private val repository: VegaGhanaQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getMtnrQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getMtnrQualityParams(materialId, valueExist, wbId)

    suspend fun getOfflineSavedQuality(materialId: String, wbId: String) =
        repository.getOfflineSavedQuality(materialId, wbId)

    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()
    suspend fun getQualityMtnrOfflineList() = repository.getQualityMtnrOfflineList()

    suspend fun postQuality(qualityPost: VegaGhanaQualityPost) = repository.postQuality(qualityPost)
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun saveVegaQualityWBDetails(wbid: String, batchno: String) =
        repository.saveVegaQualityWBDetails(wbid, batchno)

    suspend fun saveVegaQualityWeightWBDetails(wbid: String, batchno: String, weight: String) =
        repository.saveVegaQualityWeightWBDetails(wbid, batchno, weight)

    suspend fun savePostLotDetails(qualityParameter: List<VegaGhanaMtnrQualityLot>) =
        repository.savePostLotDetails(qualityParameter)

    suspend fun saveMtnrQualityData(qualityParameter: VegaGhanaQuality, batchNo: String) =
        repository.saveMtnrQualityData(qualityParameter, batchNo)

    suspend fun saveOfflineQualityData(
        qualityParameter: List<VegaQuality>, batchNo: String,
        wbid: String
    ) =
        repository.saveOfflineQualityData(qualityParameter, batchNo, wbid)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repository.saveWBDB(weighBridge)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> =
        repository.getWBWithQuality(weighBridgeID)

    suspend fun getGhanaCashewWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> =
        repository.getGhanaCashewWBWithQuality(weighBridgeID)

    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun getDeliveryBatchNumber(deliveryNo: String, posnr: String) =
        repository.getDeliveryBatchNumber(deliveryNo, posnr)

    suspend fun getOfflineDeliveryBatchNumber(deliveryNo: String, posnr: String) =
        repository.getOfflineDeliveryBatchNumber(deliveryNo, posnr)

    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun deleteWBDetals() = repository.deleteWBDetals()
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        repository.updateWBDB(wbid, charg, message, status)

    // == Start MTNR ==

    //    suspend fun getWeighBridgeDetailOnline() = repo.getWeighBridgeDetailOnline()
    suspend fun getLotDetailOnline(weighBridgeId: String) = repository.getLotDetailOnline(weighBridgeId)
    suspend fun getLotQualityDetails(weighBridgeId: String) = repository.getLotQualityDetails(weighBridgeId)
    suspend fun getAllLotQualityDetails() = repository.getAllLotQualityDetails()

    //    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
//        repo.getQualityParams(materialId, valueExist, wbId)
    suspend fun postQualityMtnr(paramPost: VegaGhanaQualityParamPost) = repository.postQuality(paramPost)

    //    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
//        repo.saveQualityData(qualityParameter, batchNo)
//    suspend fun getQualityOfflineList() = repo.getQualityOfflineList()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getOfflinePreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getOfflinePreSamplingQualityList(batchNo, materialId)
//    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repo.saveWBDB(weighBridge)

    // == End MTNR ==
}
