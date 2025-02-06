package com.olam.warehouse.vegax.qualitysesame.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualitysesame.data.api.VegaNigeriaSesameQualityApi
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityParamPost
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityPost
import com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model.VegaNigeriaSesameQualityPostResponse
import com.olam.warehouse.vegax.qualitysesame.utils.prepareData
import com.olam.warehouse.vegax.qualitysesame.utils.prepareWeighBridgeData

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
interface VegaNigeriaSesameQualityRepository {
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>

    suspend fun postQuality(qualityPost: VegaNigeriaSesameQualityPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaSesameQualityPostResponse>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int)
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>

    //== Start MTNR ==
//    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getLotDetailOnline(weighBridgeId: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>>
//    suspend fun getQualityParams(
//        materialId: String,
//        valueExist: Boolean?,
//        wbId: String?
//    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(paramPost: VegaNigeriaSesameQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaSesameQualityParamPost>>>
//    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
//    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
//    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>
    // == End MTNR ==
}

class VegaNigeriaSesameQualityRepositoryImpl(private val api: VegaNigeriaSesameQualityApi, private val dao: VegaQualityDao) : VegaNigeriaSesameQualityRepository {

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
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun postQuality(qualityPost: VegaNigeriaSesameQualityPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaSesameQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaSesameQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaSesameQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()
    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = dao.saveQualityData(qualityParameter, batchNo)
    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)
    override suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        return if (weighBridgeID.isNotEmpty()) dao.getWBWithQuality(weighBridgeID) else dao.getWBWithQuality()
    }

    override suspend fun updateDeletedItem(weighBridgeId: String) = dao.updateDeletedItem(weighBridgeId)
    override suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int) =
        dao.updateWBListStatusSuccess(wbid, charg, message, status)

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)

    override suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail> =
        dao.getThirdPartyMaterials()

    // == Start MTNR ==
//    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
//        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
//        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
//            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
//                api.fetchWeighBridgeDetail(currentKey)
//        }.build().asLiveData()
//    }

    override suspend fun getLotDetailOnline(weighBridgeId: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeLot>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeLot>> =
                api.fetchLotDetail(currentKey, weighBridgeId)
        }.build().asLiveData()
    }

//    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
//            : LiveData<List<VegaQualityParamsWithQualitative>> {
//        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
//            dao.getQualityParameter(materialId)
//    }

    override suspend fun postQuality(paramPost: VegaNigeriaSesameQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaSesameQualityParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaSesameQualityParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaSesameQualityParamPost> =
                api.postQuality(paramPost)
        }.build().asLiveData()
    }

//    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = dao.saveQualityData(qualityParameter, batchNo)

//    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)

//    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()

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
    // == END MTNR ==
}
