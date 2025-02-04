package com.olam.warehouse.vegax.grnecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnecuador.data.repo.VegaEcuadorGrnRepository

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
class VegaEcuadorGrnUseCase(
    private val repository: VegaEcuadorGrnRepository
) {
    suspend operator fun invoke(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> {
        return Transformations.map(repository.getWeighBridgeList()) {
            it // Place here your specific logic actions (if any)
        }
    }
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)

    suspend fun getStorageLocation(code: String) = repository.getStorageLocation(code)

    suspend fun postGrn(grnPost: VegaEcuadorGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> =
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
}
