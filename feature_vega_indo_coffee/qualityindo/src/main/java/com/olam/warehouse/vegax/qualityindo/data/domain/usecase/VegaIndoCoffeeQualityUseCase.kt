package com.olam.warehouse.vegax.qualityindo.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualityindo.data.repo.VegaIndoCoffeeQualityRepository

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeQualityUseCase(private val repo: VegaIndoCoffeeQualityRepository) {
    suspend fun getWeighBridgeDetailOnline() = repo.getWeighBridgeDetailOnline()
    suspend fun getLotDetailOnline(weighBridgeId: String, isDual: Boolean) =
        repo.getLotDetailOnline(weighBridgeId, isDual)

    suspend fun getLotDetailOnlineWB(weighBridgeId: String, isDual: Boolean) =
        repo.getLotDetailOnlineWB(weighBridgeId, isDual)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun postQuality(paramPost: VegaIndoCoffeeQualityParamPost) = repo.postQuality(paramPost)
    suspend fun postQualitySupplier(paramPost: VegaIndoCoffeeQualitySupplierParamPost) =
        repo.postQualitySupplier(paramPost)

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repo.saveQualityData(qualityParameter, batchNo)

    suspend fun getQualityOfflineList() = repo.getQualityOfflineList()
    suspend fun getCustomLocations() = repo.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repo.saveWBDB(weighBridge)
    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) =
        repo.getWeighBridgeIdDetail(wbid, isWeighScale)

    suspend fun getWeighBridgeDetail() = repo.getWeighBridgeDetail()
    suspend fun getLotDetailOfflineLocal(tempWbId: String) = repo.getLotDetailOfflineLocal(tempWbId)
    suspend fun saveQualityLot(lot: List<VegaCoffeeLot>) = repo.saveQualityLot(lot)
    suspend fun deleteAllItem(tmpWbId: String) = repo.deleteAllItem(tmpWbId)
    suspend fun getLotDetailOfflineQtyLocal(tempWbId: String) = repo.getLotDetailOfflineQtyLocal(tempWbId)
}
