package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.repo.VegaQualityApproveIndiaCoffeeRepository


class VegaQualityApproveIndiaCoffeeUseCase(
    private val repository: VegaQualityApproveIndiaCoffeeRepository
) {

    suspend operator fun invoke(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {
        return Transformations.map(repository.getWeighBridgeList(selectedPlantId)) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun qcinvoke(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        return Transformations.map(repository.getqcWeighBridgeList(selectedPlantId)) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun invokeQuality(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>> {
        return Transformations.map(repository.getQualityParams(charge, material)) {
            it
        }
    }

    suspend fun invokeApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>> {
        return Transformations.map(repository.lotApprove(approvePostData)) {
            it
        }
    }

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
