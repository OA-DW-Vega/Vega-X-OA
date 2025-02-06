package com.olam.warehouse.vegax.qualityapprovenigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityapprovenigeria.data.api.VegaQualityApproveNigeriaApi
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovenigeria.utils.WS
import com.olam.warehouse.vegax.qualityapprovenigeria.utils.prepareData

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
interface VegaQualityApproveNigeriaRepository {
    suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>
    suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>>

    suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun lotApprove(qualityapproveNigeriaPostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>>
    suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>>
    suspend fun openGrntDetails(
            role: String,
            materialCodes: String,
            vendorCodes: String,
            plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>>
    suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String)
    suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    )
    suspend fun postQuality(qualityPost: VegaNigeriaQcPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>>
    suspend fun getMtntWeightDetails(wbid: String,weighMethod: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>>
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>>
    suspend fun getVegaMaterials(): LiveData<List<VegaMaterial>>
//    suspend fun getMultiPlantList(): LiveData<List<Plant>>

}

class VegaApproveRepositoryImpl(private val api: VegaQualityApproveNigeriaApi, private val dao: VegaQualityDao) :
    VegaQualityApproveNigeriaRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>> =
                api.fetchWeighBridgeList(currentKey, selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchQCWeighBridgeList(currentKey, "false", selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getMaterials() = dao.getMaterials()
    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveNigeria>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getCurrentBagsIssued(materialCode:String,supplierCode:String,storageLocation:String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>> =
                api.getCurrentBagsIssued(currentKey,materialCode,supplierCode)
        }.build().asLiveData()
    }

    override suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun lotApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveNigeriaGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveNigeriaResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }

    override suspend fun openGrntDetails(
            role: String,
            materialCodes: String,
            vendorCodes: String,
            plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>> {
        return object :
                NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaGateEntryPostData>> =
                    api.fetchOpenGrntDetails(role, materialCodes, vendorCodes, plantId)
        }.build().asLiveData()
    }

    override suspend fun updateGrnNoToQuality(wbid: String, grnNo: String, batchNo: String) =
        dao.updateGrnNoToQuality(wbid, grnNo, batchNo)

    override suspend fun updateGrnSuccess(
        wbid: String,
        grnNo: String,
        batch: String,
        msg: String,
        status: Int
    ) {
        dao.updateGrnSuccess(wbid, grnNo, batch, msg, status)
    }

    override suspend fun postQuality(qualityPost: VegaNigeriaQcPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaQualityApprovePostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getMtntWeightDetails(wbid: String,weighMethod: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaWeighmentDetails> =
                    if(weighMethod.equals(WS)) {
                        api.getMtntWeightDetails(getCurrentKey(), wbid)
                    }else{
                        api.getMtntWBWeightDetails(getCurrentKey(), wbid)
                    }
        }.build().asLiveData()
    }


    override suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaWeighmentDetails>> =
                api.getWeightbridgeListDetails(getCurrentKey(),"true")
        }.build().asLiveData()
    }
//    override suspend fun getMultiPlantList() = dao.getMultiPlantList()
override suspend fun getVegaMaterials() = dao.getVegaMaterials()

}
