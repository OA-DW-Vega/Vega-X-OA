package com.olam.warehouse.vegax.bcapproveecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.data.repo.VegaEcuadorBcApproveRepository

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */
class VegaEcuadorBcApproveUseCase(private val repository: VegaEcuadorBcApproveRepository) {
    suspend fun getWeighBridgeDetailOnline(plantId:String) = repository.getWeighBridgeDetailOnline(plantId)
    suspend fun postApproval(postApprovalData: VegaEcuadorBcApprovePost): LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>> =
        repository.postApproval(postApprovalData)
    suspend fun invokeQuality(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>>> {
        return Transformations.map(repository.getQualityParams(charge, material)) {
            it
        }
    }
}
