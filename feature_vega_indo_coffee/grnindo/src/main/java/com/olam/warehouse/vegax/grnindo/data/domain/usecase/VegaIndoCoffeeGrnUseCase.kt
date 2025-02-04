package com.olam.warehouse.vegax.grnindo.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnindo.data.repo.VegaIndoCoffeeGrnRepository

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
class VegaIndoCoffeeGrnUseCase(
    private val repository: VegaIndoCoffeeGrnRepository
) {
    suspend operator fun invoke(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        return Transformations.map(repository.getWeighBridgeList()) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)

    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)

    suspend fun postGrn(grnPost: VegaEcuadorGrnPost) = repository.postGrn(grnPost)

    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getWeighBridgeTransDetail() = repository.getWeighBridgeTransDetail()
    suspend fun updateGRNPrice(wbDetails: VegaGrnWeighBridgeId) = repository.updateGRNPrice(wbDetails)
    suspend fun updateDeletedItem(weighBridgeId: String) = repository.updateDeletedItem(weighBridgeId)
    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int,
        wbDetails: VegaGrnWeighBridgeId
    ) =
        repository.updateGrnSuccess(wbid, grnNo, batch, msg, status, wbDetails)

    suspend fun getOfflineWeighBridgeDetail() = repository.getOfflineWeighBridgeDetail()
    suspend fun getOfflineWeighBridgeDetailCount() = repository.getOfflineWeighBridgeDetailCount()
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        repository.updateGrnNoToQuality(wbid, grnNo, batchNo)

    suspend fun getPOList() = repository.getPOList()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getPOListOffline() = repository.getPOListOffline()
    suspend fun deleteAllItem(wbTempId: String) = repository.deleteAllItem(wbTempId)
}
