package com.olam.warehouse.odquality.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.entity.DOMaterial
import com.olam.warehouse.master.dorigin.entity.DOQuality
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.master.dorigin.model.DOWeighBridgeWithQualityParams
import com.olam.warehouse.odquality.data.api.DOQualityApi
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.domain.model.DOQualityPostResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualitySavedResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualityWeighBridgeBagDetail
import com.olam.warehouse.odquality.utils.prepareData
import com.olam.warehouse.odquality.utils.prepareWeighBridgeData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
interface DOQualityRepository {
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<List<DOQualityWBDetails>>>
    suspend fun getQualityParams(
        materialId: String, valueExist: Boolean?, wbId: String?
    ): LiveData<List<DOQualityParamsWithQualitative>>

    suspend fun getSAPMaterials(): LiveData<List<DOMaterial>>

    suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String):LiveData<DOMaterial>


    suspend fun fetchSavedWeighBridgeDetail(plantId: String, materialCode: String, batchNumber: String): LiveData<Resource<GenericReqAndResp<DOQualitySavedResponse>>>
    suspend fun postQuality(qualityPost: DOQualityPost): LiveData<Resource<GenericReqAndResp<DOQualityPostResponse>>>
    suspend fun updateWBDB(wbid: String)
    suspend fun updateTempIdToWbid(wbid: String, grossWeight: String, tempId: String)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<DOWeighBridgeWithQualityParams>>
    suspend fun saveQualityData(qualityParameter: DOQuality, batchNo: String)
    suspend fun getQualityOfflineList(): LiveData<List<DOQualityWBDetails>>
    suspend fun getWeighBridgeDetail(): LiveData<List<DOQualityWBDetails>>
    suspend fun updateDeletedItem(wbid: String)
    suspend fun updateWBMessage(msg: String, weighBridgeId: String?)
    suspend fun getQualityWeighBridgeBagDetail(key:String, plantId: String): LiveData<Resource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>>
}

class DOQualityRepositoryImpl(private val api: DOQualityApi, private val dao: DOQualityDao) : DOQualityRepository {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getSAPMaterials() = dao.getSAPMaterials()

    override suspend fun updateWBMessage(msg: String, weighBridgeId: String?) = dao.updateWBMessage(msg, weighBridgeId)

    override suspend fun updateDeletedItem(wbid: String) = dao.updateDeletedItem(wbid)

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList(currentKey)

    override suspend fun saveQualityData(qualityParameter: DOQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun getWBWithQuality(weighBridgeID: String) = dao.getWBWithQualityAll(currentKey)

    override suspend fun updateTempIdToWbid(wbid: String, grossWeight: String, tempId: String) =
        dao.updateTempIdToWbid(wbid, grossWeight, tempId)

    override suspend fun updateWBDB(wbid: String) = dao.updateWBDB(wbid)

    override suspend fun fetchSavedWeighBridgeDetail(plantId: String, materialCode: String, batchNumber: String)
            : LiveData<Resource<GenericReqAndResp<DOQualitySavedResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOQualitySavedResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<DOQualitySavedResponse> =
                api.fetchSavedWeighBridgeDetail(plantId, materialCode, batchNumber, currentKey)
        }.build().asLiveData()
    }

    override suspend fun getQualityWeighBridgeBagDetail(key: String, plantId: String)
            : LiveData<Resource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOQualityWeighBridgeBagDetail>>() {
            override suspend fun createCall(): GenericReqAndResp<DOQualityWeighBridgeBagDetail> =
                api.getDOWeighBridgeBagDetails(currentKey, plantId)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<DOQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun postQuality(qualityPost: DOQualityPost): LiveData<Resource<GenericReqAndResp<DOQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<DOQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<List<DOQualityWBDetails>>> {

        return object : NetworkBoundResource<List<DOQualityWBDetails>, GenericReqAndResp<List<DOQualityWBDetails>>>() {

            override fun processResponse(response: GenericReqAndResp<List<DOQualityWBDetails>>): List<DOQualityWBDetails> =
                response.data

            override suspend fun saveCallResults(items: List<DOQualityWBDetails>) = dao.save(items)

            override fun shouldFetch(data: List<DOQualityWBDetails>?): Boolean = true

            override suspend fun loadFromDb(): List<DOQualityWBDetails> =
                dao.getWeighBridgeDetailOnline(currentKey)

            override suspend fun createCall(): GenericReqAndResp<List<DOQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetail(): LiveData<List<DOQualityWBDetails>> {
        val receiveItem = dao.getReceivingDetail(currentKey)
        receiveItem.forEach {
            if (dao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
            dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail(currentKey)
    }

    override suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String) = dao.getSAPMaterialsUsingMaterialCode(materialCode)


}
