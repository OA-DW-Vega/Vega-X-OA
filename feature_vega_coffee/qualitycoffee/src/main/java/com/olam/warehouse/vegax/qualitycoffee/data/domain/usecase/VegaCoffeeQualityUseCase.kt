package com.olam.warehouse.vegax.qualitycoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaSavePrintTicket
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.repo.VegaCoffeeQualityRepository

class VegaCoffeeQualityUseCase(private val repo: VegaCoffeeQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repo.getWeighBridgeDetailOnline()
    suspend fun getLotDetailOnline(weighBridgeId: String, isDual: Boolean) =
        repo.getLotDetailOnline(weighBridgeId, isDual)

    suspend fun getLotDetailOnlineWB(weighBridgeId: String, isDual: Boolean) =
        repo.getLotDetailOnlineWB(weighBridgeId, isDual)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost) = repo.postQuality(paramPost)
    suspend fun postQualitySupplier(paramPost: VegaCoffeeQualitySupplierParamPost) =
        repo.postQualitySupplier(
            paramPost
        )

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repo.saveQualityData(qualityParameter, batchNo)

    suspend fun getQualityOfflineList() = repo.getQualityOfflineList()
    suspend fun getCustomLocations() = repo.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repo.saveWBDB(weighBridge)
    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) =
        repo.getWeighBridgeIdDetail(wbid, isWeighScale)

    suspend fun updateTallySequence(post: NicaraguaUpdateTallySequencePost) =
        repo.updateTallySequence(post)
    suspend fun getMaterialQualityGrades(materialCode: String) = repo.getMaterialQualityGrades(materialCode)
    suspend fun getGrades(materialCode: String) = repo.getGrades(materialCode)

    suspend fun postprintTicket(postPrintData : NicaraguaSavePrintTicket) =
        repo.postprintformat(postPrintData)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repo.getConfigItems(role)

    suspend fun getMaterialDetails(materialCode: String) = repo.getMaterialDetails(materialCode)


}
