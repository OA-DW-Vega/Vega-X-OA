package com.olam.warehouse.vegax.qualityecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.Material
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.dorigin.entity.DOMaterial
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorQualitySavedResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityecuador.data.api.VegaEcuadorQualityApi
import com.olam.warehouse.vegax.qualityecuador.utils.prepareData
import com.olam.warehouse.vegax.qualityecuador.utils.prepareWeighBridgeData

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
interface VegaEcuadorQualityRepository {
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun fetchSavedWeighBridgeDetail(
        plantId: String,
        materialCode: String,
        batchNumber: String
    ): LiveData<Resource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>>

    suspend fun postQuality(qualityPost: VegaQualityPost): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int)

    suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String):LiveData<VegaMaterial>

}

class VegaEcuadorQualityRepositoryImpl(private val api: VegaEcuadorQualityApi, private val dao: VegaQualityDao) : VegaEcuadorQualityRepository {

    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>> {
        val receiveItem = dao.getGrnDetail()
        receiveItem.forEach {
            if (dao.isWBExist(it.wbTempId).isNotEmpty()) return@forEach
            dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                api.fetchWeighBridgeDetail(currentKey, true)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun postQuality(qualityPost: VegaQualityPost): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()
    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)
    override suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        return if (weighBridgeID.isNotEmpty()) dao.getWBWithQuality(weighBridgeID) else dao.getWBWithQuality()
    }

    override suspend fun updateDeletedItem(weighBridgeId: String) = dao.updateDeletedItem(weighBridgeId)
    override suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        dao.updateWBListStatusSuccess(wbid, charg, message, status)

    override suspend fun fetchSavedWeighBridgeDetail(plantId: String, materialCode: String, batchNumber: String)
            : LiveData<Resource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorQualitySavedResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorQualitySavedResponse> =
                api.fetchSavedWeighBridgeDetail(plantId, materialCode, batchNumber, currentKey)
        }.build().asLiveData()
    }

    override suspend fun getSAPMaterialsUsingMaterialCode(materialCode: String) = dao.getSAPMaterialsUsingMaterialCode(materialCode)

}
