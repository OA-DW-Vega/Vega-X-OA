package com.olam.warehouse.vegax.bcapprovecameroon.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.bcapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.bcapprovecameroon.data.repo.VegaQualityApproveCameroonRepository

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaBcApproveCameroonUseCase(
    private val repository: VegaQualityApproveCameroonRepository
) {
    suspend fun fetchWeighBridgeList() = repository.getWeighBridgeList()

    suspend fun fetchQualityDetails(charge: String, material: String) = repository.getQualityParams(charge, material)

    suspend fun wbIdLotApprove(approvePostData: VegaQualityApproveCameroonPostData) = repository.lotApprove(approvePostData)

    suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonResponse>>> =
        repository.postApproval(postApprovalData)

    suspend fun postQuality(qualityPost: VegaCameroonQualityApprovePost) =
        repository.postQuality(qualityPost)

    suspend fun getQualityParam(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParam(materialId, valueExist, wbId)

    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>> =
        repository.getMtntWeightDetails(wbid)

    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>> =
        repository.getWeightbridgeListDetails()

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)


}
