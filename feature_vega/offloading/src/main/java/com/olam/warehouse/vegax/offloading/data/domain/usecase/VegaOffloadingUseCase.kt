package com.olam.warehouse.vegax.offloading.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingQualityPost
import com.olam.warehouse.vegax.offloading.data.repo.VegaOffloadingRepository

class VegaOffloadingUseCase(private val repository:VegaOffloadingRepository) {

    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun postQuality(qualityPost: VegaOffloadingQualityPost) = repository.postQuality(qualityPost)
    suspend fun updateDB(wbid: String) = repository.updateDB(wbid)
    suspend fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) =
        repository.saveQualityData(qualityParameter, batchNo)

    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getDeliveryBatchNumber(deliveryNo: String, posnr: String) =
        repository.getDeliveryBatchNumber(deliveryNo, posnr)

    suspend fun getQualityParams(charge: String, material: String) = repository.getQualityParams(charge, material)
    suspend fun getSuggestedLocation(kor: String, origin: String, materialCode: String) =
        repository.getSuggestedLocation(kor, origin, materialCode)
}

