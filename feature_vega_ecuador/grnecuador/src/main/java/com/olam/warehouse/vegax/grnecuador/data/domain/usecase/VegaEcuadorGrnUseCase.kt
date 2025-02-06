package com.olam.warehouse.vegax.grnecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.grnecuador.data.repo.VegaEcuadorGrnRepository

/**
 * Created by Roshna Parambil on 1/13/2022.
 */
class VegaEcuadorGrnUseCase(
    private val repository: VegaEcuadorGrnRepository
) {
    suspend fun fetchWeighBridgeList() = repository.getWeighBridgeList()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun getWeighBridgeGrnPendingList(): LiveData<Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>> = repository.getWeighBridgeGrnPendingList()

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
    suspend fun getPOList() = repository.getPOList()
    suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String) = repository.getSAPMaterialsUsingMaterialCode(materialCode)
    suspend fun getFeatureMaster(module: String) = repository.getFeatureMaster(module)

}
