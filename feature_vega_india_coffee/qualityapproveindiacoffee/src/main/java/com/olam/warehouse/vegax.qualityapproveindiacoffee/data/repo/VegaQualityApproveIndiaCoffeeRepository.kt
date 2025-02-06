package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.api.VegaQualityApproveIndiaCoffeeApi
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.prepareData


interface VegaQualityApproveIndiaCoffeeRepository {
    suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>>
    suspend fun getqcWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>>

    suspend fun getQualityParam(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun lotApprove(qualityapprovecameroonPostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>>
    suspend fun postGrn(grnPost: VegaQualityApproveIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>>
    suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>>
    suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>>
    suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>>
    suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
//    suspend fun getMultiPlantList(): LiveData<List<Plant>>

}

class VegaApproveRepositoryImpl(private val api: VegaQualityApproveIndiaCoffeeApi, private val dao: VegaQualityDao) :
    VegaQualityApproveIndiaCoffeeRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWeighBridgeList(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>> =
                api.fetchWeighBridgeList(currentKey, selectedPlantId)

        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
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
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>> =
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

    override suspend fun lotApprove(approvePostData: VegaQualityApproveCameroonPostData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId> =
                api.approveWbid(approvePostData)

        }.build().asLiveData()
    }

    override suspend fun postGrn(grnPost: VegaQualityApproveIndiaCoffeeGrnPost): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse> =
                api.postGrn(grnPost)
        }.build().asLiveData()
    }

    override suspend fun postApproval(postApprovalData: PostApprovalData): LiveData<Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse> =
                api.postApproval(postApprovalData)
        }.build().asLiveData()
    }

    override suspend fun postQuality(qualityPost: VegaCameroonQcPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }
    override suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getMtntWeightDetails(wbid: String): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails> =
                api.getMtntWeightDetails(getCurrentKey(),wbid)
        }.build().asLiveData()
    }


    override suspend fun getWeightbridgeListDetails(): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>> =
                api.getWeightbridgeListDetails(getCurrentKey(),"true")
        }.build().asLiveData()
    }
//    override suspend fun getMultiPlantList() = dao.getMultiPlantList()

}
