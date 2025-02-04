package com.olam.warehouse.vegax.qualitycoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeQualityDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualitycoffee.data.api.VegaCoffeeQualityApi
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualitycoffee.utils.prepareData

interface VegaCoffeeQualityRepository {
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getLotDetailOnline(
        weighBridgeId: String,
        isDual: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>>

    suspend fun getLotDetailOnlineWB(
        weighBridgeId: String,
        isDual: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaCoffeeLot>>>

    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>>
    suspend fun postQualitySupplier(paramPost: VegaCoffeeQualitySupplierParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>

    suspend fun updateTallySequence(postData: NicaraguaUpdateTallySequencePost): LiveData<Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>>
}

class VegaCoffeeQualityRepositoryImpl(private val api: VegaCoffeeQualityApi, private val dao: VegaCoffeeQualityDao) :
    VegaCoffeeQualityRepository {
    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getLotDetailOnline(
        weighBridgeId: String,
        isDual: Boolean
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeLot>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeLot>> =
                api.fetchLotDetail(
                    currentKey,
                    weighBridgeId
                )
        }.build().asLiveData()
    }

    override suspend fun getLotDetailOnlineWB(
        weighBridgeId: String,
        isDual: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaCoffeeLot>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeLot>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeLot> =
                api.fetchLotDetailForDual(currentKey, weighBridgeId)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeQualityParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeQualityParamPost> =
                api.postQuality(paramPost)
        }.build().asLiveData()
    }

    override suspend fun postQualitySupplier(paramPost: VegaCoffeeQualitySupplierParamPost): LiveData<Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeQualitySupplierParamPost> =
                api.postQualitySupplier(paramPost)
        }.build().asLiveData()
    }

    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }
    override suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                if (isWeighscale) api.getWeighScaleIdDetail(
                    getCurrentKey(),
                    wbid
                ) else api.getWeighBridgeIdDetail(getCurrentKey(), wbid)
        }.build().asLiveData()
    }

    override suspend fun updateTallySequence(postData: NicaraguaUpdateTallySequencePost): LiveData<Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>() {

            override suspend fun createCall(): GenericReqAndResp<NicaraguaUpdateTallySequencePost> =
                api.updateLotSequence(postData)
        }.build().asLiveData()
    }
}
