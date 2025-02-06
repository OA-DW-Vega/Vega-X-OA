package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovecameroon.data.repo.VegaQualityApproveCameroonRepository

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
class VegaQualityApproveCameroonUseCase(
    private val repository: VegaQualityApproveCameroonRepository
) {

    suspend fun fetchWeighBridgeList(selectedPlantId: String) = repository.getWeighBridgeList(selectedPlantId)

    suspend fun fetchQualityDetails(charge: String, material: String) = repository.getQualityParams(charge, material)

    suspend fun wbIdLotApprove(approvePostData: VegaQualityApproveCameroonPostData) = repository.lotApprove(approvePostData)

    suspend fun fetchQcWeighBridgeList(selectedPlantId: String) = repository.getqcWeighBridgeList(selectedPlantId)

    suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>> =
        repository.postGrn(grnPost)

    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>> =
        repository.postApproval(postApprovalData)

    suspend fun postQuality(qualityPost: VegaCameroonQcPost) = repository.postQuality(qualityPost)

    suspend fun getQualityParam(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParam(materialId, valueExist, wbId)

    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>> =
        repository.getMtntWeightDetails(wbid)

    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>> =
        repository.getWeightbridgeListDetails()

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()

    //get DSE Response Values
    suspend fun getDSEResponse(
        status: String
    ): LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>> =
        repository.getDSEResponse(status)

    //get Threshold Values
    suspend fun getThresholdValue(role: String):
            LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getThresholdValue(role)

    suspend fun getReprintList() = repository.getReprintList()

    suspend fun downloadReprintItem(selectedItemId: String) = repository.downloadRePrintItem(selectedItemId)


}
