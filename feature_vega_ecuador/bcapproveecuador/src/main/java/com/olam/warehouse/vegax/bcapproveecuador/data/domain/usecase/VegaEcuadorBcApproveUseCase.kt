package com.olam.warehouse.vegax.bcapproveecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorBcApprovePostResponse
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorPostApprovalRequest
import com.olam.warehouse.vegax.bcapproveecuador.data.repo.VegaEcuadorBcApproveRepository

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */
class VegaEcuadorBcApproveUseCase(private val repository: VegaEcuadorBcApproveRepository) {
    suspend fun getWeighBridgeDetailOnline(plantId:String) = repository.getWeighBridgeDetailOnline(plantId)
    suspend fun postApproval(postApprovalData: VegaEcuadorBcApprovePost): LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApproveResponse>>> =
        repository.postApproval(postApprovalData)
    suspend fun postApproval(postApprovalData: VegaEcuadorPostApprovalRequest): LiveData<Resource<GenericReqAndResp<VegaEcuadorBcApprovePostResponse>>> =
        repository.postApproval(postApprovalData)
    suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ) = repository.getQualityParams(charge, material)
    suspend fun getFeatureMaster(module: String) = repository.getFeatureMaster(module)
}
