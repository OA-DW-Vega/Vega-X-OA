package com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityParamPost
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityPost
import com.olam.warehouse.vegax.qualitycameroon.data.repo.VegaCameroonQualityRepository

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
class VegaCameroonQualityUseCase(private val repository: VegaCameroonQualityRepository) {
    suspend fun getWeighBridgeDetailOnline(qcFlag: String) = repository.getWeighBridgeDetailOnline(qcFlag)
    suspend fun getMtnrWeighBridgeDetailOnline() = repository.getMtnrWeighBridgeDetailOnline()
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getQualityOfflineList() = repository.getQualityOfflineList()

    suspend fun postQuality(qualityPost: VegaCameroonQualityPost) = repository.postQuality(qualityPost)
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

    suspend fun getLotDetailOnline(weighBridgeId: String) = repository.getLotDetailOnline(weighBridgeId)

    suspend fun postQualityMtnr(paramPost: VegaCameroonQualityParamPost) = repository.postQuality(paramPost)


    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    // == End MTNR ==

    suspend fun getDashboardResult(startDate: String, endDate: String, isMtnt: Boolean) =
        repository.getDashboardResult(startDate, endDate, isMtnt)

    suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String) = repository.getSAPMaterialsUsingMaterialCode(materialCode)


}
