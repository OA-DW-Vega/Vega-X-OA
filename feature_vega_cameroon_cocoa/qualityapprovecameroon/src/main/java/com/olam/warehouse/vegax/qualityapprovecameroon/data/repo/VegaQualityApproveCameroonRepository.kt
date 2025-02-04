package com.olam.warehouse.vegax.qualityapprovecameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityapprovecameroon.data.api.VegaQualityApproveCameroonApi
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovecameroon.utils.prepareData

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */
interface VegaQualityApproveCameroonRepository {
    suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>
    suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>>

    suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getThresholdValue(
        role: String
    ): LiveData<List<VegaCocoaMiscellaneous>>


    suspend fun lotApprove(qualityapprovecameroonPostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>>
    suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>>
    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>>
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

    //ADD get call to Repository
    suspend fun getDSEResponse(
        status: String
    ): LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>>

}

class VegaApproveRepositoryImpl(
    private val api: VegaQualityApproveCameroonApi,
    private val dao: VegaQualityDao
) :
    VegaQualityApproveCameroonRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>> =
                api.fetchWeighBridgeList(currentKey, selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchQCWeighBridgeList(currentKey, "true", selectedPlantId)

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

    //Threshold Response
    override suspend fun getThresholdValue(
        role: String
    ): LiveData<List<VegaCocoaMiscellaneous>> = dao.getThresholdValue(role)

    override suspend fun lotApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaQualityApproveCameroonGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveCameroonGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveCameroonResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }

    override suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>> {
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

    // Added Get Call to DSE Repository Data
    override suspend fun getDSEResponse(status: String): LiveData<Resource<GenericReqAndResp<VegaQualityApproveDSE>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveDSE>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveDSE> =
                api.getDSEResponse(getCurrentKey(), status)
        }.build().asLiveData()
    }

}
