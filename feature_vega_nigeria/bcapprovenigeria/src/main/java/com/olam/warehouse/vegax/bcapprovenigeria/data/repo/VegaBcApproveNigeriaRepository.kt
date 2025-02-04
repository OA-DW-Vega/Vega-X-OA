package com.olam.warehouse.vegax.bcapprovenigeria.data.repo

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
import com.olam.warehouse.vegax.bcapprovenigeria.data.api.VegaBCApproveNigeriaApi
import com.olam.warehouse.vegax.bcapprovenigeria.data.domain.model.*
import com.olam.warehouse.vegax.bcapprovenigeria.utils.prepareData

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
interface VegaQualityApproveNigeriaRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>>
    suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun lotApprove(qualityapprovecameroonPostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaBcApproveNigeriaGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaBcApproveNigeriaResponse>>>
    suspend fun postQuality(qualityPost: VegaNigeriaQualityApprovePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>>
    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>>
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

}

class VegaApproveRepositoryImpl(private val api: VegaBCApproveNigeriaApi, private val dao: VegaQualityDao) :
    VegaQualityApproveNigeriaRepository {

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
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveNigeria>> =
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

    override suspend fun lotApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaQualityApproveNigeriaGrnPost): LiveData<Resource<GenericReqAndResp<VegaBcApproveNigeriaGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaBcApproveNigeriaGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaBcApproveNigeriaGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaBcApproveNigeriaResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaBcApproveNigeriaResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaBcApproveNigeriaResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }

    override suspend fun postQuality(qualityPost: VegaNigeriaQualityApprovePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaQualityApprovePostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaWeighmentDetails> =
                api.getMtntWeightDetails(getCurrentKey(),wbid)
        }.build().asLiveData()
    }


    override suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaWeighmentDetails>> =
                api.getWeightbridgeListDetails(getCurrentKey(),"true")
        }.build().asLiveData()
    }

}
