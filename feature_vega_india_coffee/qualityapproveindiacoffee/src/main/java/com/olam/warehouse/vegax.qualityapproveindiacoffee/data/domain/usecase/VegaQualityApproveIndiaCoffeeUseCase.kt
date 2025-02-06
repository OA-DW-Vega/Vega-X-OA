package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.repo.VegaQualityApproveIndiaCoffeeRepository


class VegaQualityApproveIndiaCoffeeUseCase(
    private val repository: VegaQualityApproveIndiaCoffeeRepository
) {

    suspend fun fetchWeighBridgeList(selectedPlantId: String) = repository.getWeighBridgeList(selectedPlantId)
    suspend fun fetchQcWeighBridgeList(selectedPlantId: String) = repository.getqcWeighBridgeList(selectedPlantId)

    suspend fun fetchQualityDetails(charge: String, material: String) = repository.getQualityParams(charge, material)
    suspend fun wbIdLotApprove(approvePostData: VegaQualityApproveCameroonPostData) = repository.lotApprove(approvePostData)

    suspend fun postGrn(grnPost: VegaQualityApproveIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>> =
        repository.postApproval(postApprovalData)

    suspend fun postQuality(qualityPost: VegaCameroonQcPost) = repository.postQuality(qualityPost)

    suspend fun getQualityParam(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParam(materialId, valueExist, wbId)

    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighscale: Boolean) =
        repository.getWeighBridgeIdDetail(wbid, isWeighscale)

    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>>  = repository.getMtntWeightDetails(wbid)
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>>  = repository.getWeightbridgeListDetails()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()



//    suspend fun getMultiPlantList() = repository.getMultiPlantList()


}
