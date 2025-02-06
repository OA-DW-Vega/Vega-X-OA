package com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
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

    suspend fun fetchWeighBridgeList(selectedPlantId: String) = repository.getWeighBridgeList(selectedPlantId)

    suspend fun fetchQcWeighBridgeList(selectedPlantId: String) = repository.getqcWeighBridgeList(selectedPlantId)
    suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ) = repository.getQualityParams(charge, material)

    suspend fun wbIdLotApprove(approvePostData: VegaQualityApproveCameroonPostData) = repository.lotApprove(approvePostData)

    suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>> =
        repository.postGrn(grnPost)
    suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> =
        repository.postGrn(grnPost)
    suspend fun openGrntDetails(
            role: String,
            materialCodes: String,
            vendorCodes: String,
            plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>> =
            repository.openGrntDetails(role, materialCodes, vendorCodes, plantId)
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>> =
        repository.postApproval(postApprovalData)
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        repository.updateGrnNoToQuality(wbid, grnNo, batchNo)
    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    ) =
        repository.updateGrnSuccess(wbid, grnNo, batch, msg, status)
    suspend fun postQuality(qualityPost: VegaNigeriaQcPost) = repository.postQuality(qualityPost)

    suspend fun getQualityParam(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParam(materialId, valueExist, wbId)
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getMtntWeightDetails(wbid: String,weighMethod: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>>  = repository.getMtntWeightDetails(wbid,weighMethod)
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>>  = repository.getWeightbridgeListDetails()
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>> = repository.getCurrentBagsIssued(materialCode,supplierCode,storageLocation)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun getVegaMaterials() = repository.getVegaMaterials()
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()


}
