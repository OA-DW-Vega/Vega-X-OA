package com.olam.warehouse.vegax.approve.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.approve.data.api.VegaApproveApi
import com.olam.warehouse.vegax.approve.data.domain.model.*

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
interface VegaApproveRepository {
    suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>>

    suspend fun lotApprove(approvePostData: VegaApprovePostData): LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaGrnPost): LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>>
}

class VegaApproveRepositoryImpl(private val api: VegaApproveApi) : VegaApproveRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(): LiveData<Resource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaApproveWeighBridgeId>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaApproveWeighBridgeId>> =
                api.fetchWeighBridgeList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaApproveQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaApproveQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaApproveQuality>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun lotApprove(approvePostData: VegaApprovePostData): LiveData<Resource<GenericReqAndResp<VegaApproveWeighBridgeId>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaApproveWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaApproveWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaGrnPost): LiveData<Resource<GenericReqAndResp<VegaGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaApprovalResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaApprovalResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaApprovalResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }
}
