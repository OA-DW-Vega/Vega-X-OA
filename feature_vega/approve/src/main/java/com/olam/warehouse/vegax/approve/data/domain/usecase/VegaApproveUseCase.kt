package com.olam.warehouse.vegax.approve.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.approve.data.domain.model.*
import com.olam.warehouse.vegax.approve.data.repo.VegaApproveRepository

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaApproveUseCase(
    private val repository: VegaApproveRepository
) {

    suspend operator fun invoke(): LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>> {
        return Transformations.map(repository.getWeighBridgeList()) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun invokeQuality(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>> {
        return Transformations.map(repository.getQualityParams(charge, material)) {
            it
        }
    }

    suspend fun invokeApprove(approvePostData: VegaApprovePostData): LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>> {
        return Transformations.map(repository.lotApprove(approvePostData)) {
            it
        }
    }

    suspend fun postGrn(grnPost: VegaGrnPost): LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>> =
        repository.postApproval(postApprovalData)


}
