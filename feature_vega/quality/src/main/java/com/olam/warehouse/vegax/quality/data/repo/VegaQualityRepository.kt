package com.olam.warehouse.vegax.quality.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.quality.data.api.VegaQualityApi
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPost
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPostResponse
import com.olam.warehouse.vegax.quality.utils.prepareData
import com.olam.warehouse.vegax.quality.utils.prepareWeighBridgeData

interface VegaQualityRepository {
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(qualityPost: VegaQualityPost): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun updateDeletedItem(wbid: String)
    //suspend fun getOfflineParamItems(wbid: String): LiveData<List<Quality>>
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>

    suspend fun updateTempIdToWbid(wbid: String, tempId: String)
    suspend fun updateWBMessage(msg: String, weighBridgeId: String?)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getPostProcessingQualityWBDetails(): LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>>

}



class VegaQualityRepositoryImpl(private val api: VegaQualityApi, private val dao: VegaQualityDao) : VegaQualityRepository {

    override suspend fun updateTempIdToWbid(wbid: String, tempId: String) = dao.updateTempIdToWbid(wbid, tempId)


    override suspend fun updateDeletedItem(wbid: String) = dao.updateDeletedItem(wbid)
    override suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        dao.updateWBListStatusSuccess(wbid, charg, message, status)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)
    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>> {
        val receiveItem = dao.getReceivingDetail()
        receiveItem.forEach {
            if (dao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
                 dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()
    //override suspend fun getOfflineParamItems(wbid: String) = dao.getOfflineParamItems(wbid)
    override suspend fun getWBWithQuality(weighBridgeID: String) = dao.getWBWithQualityAll()

    override suspend fun postQuality(qualityPost: VegaQualityPost): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }


    /* override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<List<VegaQualityWBDetails>>> {
         val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
         return object : NetworkBoundResource<List<VegaQualityWBDetails>, GenericReqAndResp<List<VegaQualityWBDetails>>>() {

             override fun processResponse(response: GenericReqAndResp<List<VegaQualityWBDetails>>): List<VegaQualityWBDetails> =
                 response.data

             override suspend fun saveCallResults(items: List<VegaQualityWBDetails>) = dao.save(items)

             override fun shouldFetch(data: List<VegaQualityWBDetails>?): Boolean = true

             override suspend fun loadFromDb(): List<VegaQualityWBDetails> = dao.getWeighBridgeDetailOnline()

             override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                 api.fetchWeighBridgeDetail(currentKey)

         }.build().asLiveData()
     }*/

    override suspend fun updateWBMessage(msg: String, weighBridgeId: String?) {
        dao.updateWBMessage(msg, weighBridgeId)
    }

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

    override suspend fun getPostProcessingQualityWBDetails(): LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaPostProcessQualityWBDetails>> =
                api.fetchPostProcessQualityDetails(currentKey)
        }.build().asLiveData()
    }
}
