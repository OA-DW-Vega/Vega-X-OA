package com.olam.warehouse.vegax.approve.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
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

    suspend fun fetchWeighBridgeList() = repository.getWeighBridgeList()

    suspend fun fetchQualityDetails(charge: String, material: String) = repository.getQualityParams(charge, material)

    suspend fun wbIdLotApprove(approvePostData: VegaApprovePostData) = repository.lotApprove(approvePostData)
    suspend fun postGrn(grnPost: VegaGrnPost): LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>> =
        repository.postApproval(postApprovalData)


}
