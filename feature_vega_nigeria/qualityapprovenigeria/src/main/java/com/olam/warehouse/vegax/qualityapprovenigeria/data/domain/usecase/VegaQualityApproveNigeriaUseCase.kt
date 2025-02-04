package com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovenigeria.data.repo.VegaQualityApproveNigeriaRepository

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaQualityApproveNigeriaUseCase(
    private val repository: VegaQualityApproveNigeriaRepository
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
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> {
        return Transformations.map(repository.getQualityParams(charge, material)) {
            it
        }
    }

    suspend fun invokeApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>> {
        return Transformations.map(repository.lotApprove(approvePostData)) {
            it
        }
    }

    suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>> =
        repository.postApproval(postApprovalData)

    suspend fun postQuality(qualityPost: VegaNigeriaQcPost) = repository.postQuality(qualityPost)

    suspend fun getQualityParam(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParam(materialId, valueExist, wbId)

    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>>  = repository.getMtntWeightDetails(wbid)
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>>  = repository.getWeightbridgeListDetails()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()


}
