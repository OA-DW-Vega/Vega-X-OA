package com.olam.warehouse.vegax.qualityindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityindiacoffee.data.api.VegaIndiaCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPost
import com.olam.warehouse.vegax.qualityindiacoffee.data.domain.model.VegaIndiaCoffeeQualityPostResponse
import com.olam.warehouse.vegax.qualityindiacoffee.utils.prepareData
import com.olam.warehouse.vegax.qualityindiacoffee.utils.prepareWeighBridgeData

interface VegaIndiaCoffeeQualityRepository {
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(qualityPost: VegaIndiaCoffeeQualityPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int)
    suspend fun updateDB(wbid: String)
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

    suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>

    suspend fun getPostProcessingQualityWBDetails(): LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>>
    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>

}


class VegaQualityRepositoryImpl(
    private val apiIndiaCoffee: VegaIndiaCoffeeQualityApi,
    private val dao: VegaQualityDao
) : VegaIndiaCoffeeQualityRepository {

    override suspend fun updateTempIdToWbid(wbid: String, tempId: String) = dao.updateTempIdToWbid(wbid, tempId)


    override suspend fun updateDeletedItem(wbid: String) = dao.updateDeletedItem(wbid)
    override suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        dao.updateWBListStatusSuccess(wbid, charg, message, status)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)
    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

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

    override suspend fun postQuality(qualityPost: VegaIndiaCoffeeQualityPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeQualityPostResponse> =
                apiIndiaCoffee.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                apiIndiaCoffee.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                apiIndiaCoffee.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                    apiIndiaCoffee.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun updateDB(wbid: String) = dao.updateDB(wbid)


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
                apiIndiaCoffee.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun getPostProcessingQualityWBDetails(): LiveData<Resource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaPostProcessQualityWBDetails>> =
                apiIndiaCoffee.fetchPostProcessQualityDetails(currentKey)
        }.build().asLiveData()
    }
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeRminLots>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeRminLots>> =
                apiIndiaCoffee.getStocks(currentKey, material)
        }.build().asLiveData()
    }
}
