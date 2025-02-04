package com.olam.warehouse.vegax.bcapprovecameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.bcapprovecameroon.data.api.VegaBCApproveCameroonApi
import com.olam.warehouse.vegax.bcapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.bcapprovecameroon.utils.prepareData

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
interface VegaQualityApproveCameroonRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>>
    suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun lotApprove(qualityapprovecameroonPostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonResponse>>>
    suspend fun postQuality(qualityPost: VegaCameroonQualityApprovePost): LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>>
    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>>
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    /*//ADD get call DSE to Repository
    suspend fun getDSEResponse(dwStatus: String): LiveData<Resource<GenericReqAndResp<VegaBCApproveCameroonDSEData>>>
*/
}

class VegaApproveRepositoryImpl(private val api: VegaBCApproveCameroonApi, private val dao: VegaQualityDao) :
    VegaQualityApproveCameroonRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>> =
                api.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveCameroon>> =
                api.getQuality(currentKey, charge, material)

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

    override suspend fun lotApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaBcApproveCameroonGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaBcApproveCameroonGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaBcApproveCameroonResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaBcApproveCameroonResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaBcApproveCameroonResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }

    override suspend fun postQuality(qualityPost: VegaCameroonQualityApprovePost): LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCameroonQualityApprovePostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonWeighmentDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonWeighmentDetails> =
                api.getMtntWeightDetails(getCurrentKey(), wbid)
        }.build().asLiveData()
    }


    override suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonWeighmentDetails>> =
                api.getWeightbridgeListDetails(getCurrentKey(), "true")
        }.build().asLiveData()
    }

    /*// Added Get Call to DSE Repository
    override suspend fun getDSEResponse(dwStatus: String): LiveData<Resource<GenericReqAndResp<VegaBCApproveCameroonDSEData>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaBCApproveCameroonDSEData>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaBCApproveCameroonDSEData> =
                api.getDSEResponse(getCurrentKey(), dwStatus)
        }.build().asLiveData()
    }*/
}
