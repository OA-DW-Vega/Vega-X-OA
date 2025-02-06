package com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaCameroonQcPost
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGRNQuality
import com.olam.warehouse.vegax.grnindiacoffee.data.domain.usecase.model.VegaIndiaCoffeeGrnPost
import com.olam.warehouse.vegax.grnindiacoffee.data.repo.VegaIndiaCoffeeGrnRepository


class VegaIndiaCoffeeGrnUseCase(
    private val repository: VegaIndiaCoffeeGrnRepository
) {
    suspend fun fetchWeighBridgeList() = repository.getWeighBridgeList()

    suspend fun fetchQualityDetails(charge: String, material: String) = repository.getQualityParams(charge, material)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)

    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)

    suspend fun getCustomLocations() = repository.getCustomLocations()

    suspend fun postGrn(grnPost: VegaIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun getWeighBridgeDetail(): LiveData<List<VegaGrnWeighBridgeId>> = repository.getWeighBridgeDetail()
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = repository.updateGRNPrice(wbDetails)
    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun updateGrnSuccess(wbid: String, grnNo: String, batch: String, msg: String, status: Int) =
        repository.updateGrnSuccess(wbid, grnNo, batch, msg, status)

    suspend fun getOfflineWeighBridgeDetail() = repository.getOfflineWeighBridgeDetail()
    suspend fun getOfflineWeighBridgeDetailCount() = repository.getOfflineWeighBridgeDetailCount()
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        repository.updateGrnNoToQuality(wbid, grnNo, batchNo)
    suspend fun getPOList() = repository.getPOList()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postQuality(qualityPost: VegaCameroonQcPost) = repository.postQuality(qualityPost)
}
