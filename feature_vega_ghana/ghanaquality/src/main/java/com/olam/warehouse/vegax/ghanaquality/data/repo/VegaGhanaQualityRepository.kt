package com.olam.warehouse.vegax.ghanaquality.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.user.model.VegaCoffeeThirdPartyMaterialDetail
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaQualityWithQualitative
import com.olam.warehouse.master.vega.model.VegaWeighBridgeWithQualityParams
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaLotQualityDetails
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQuality
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaQualityMtnBatch
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ghanaquality.data.api.VegaGhanaQualityApi
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.BatchNumResponse
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityParamPost
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityPost
import com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model.VegaGhanaQualityPostResponse
import com.olam.warehouse.vegax.ghanaquality.utils.prepareData
import com.olam.warehouse.vegax.ghanaquality.utils.prepareWeighBridgeData

/**
 * Created by Keerthi Santhanam on 6/21/2020.
 */
interface VegaGhanaQualityRepository {
    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getMtnrQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getOfflineSavedQuality(
        materialId: String,
        wbId: String?
    ): LiveData<List<VegaQualityWithQualitative>>

    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getQualityMtnrOfflineList(): LiveData<List<VegaQualityWBDetails>>

    suspend fun postQuality(qualityPost: VegaGhanaQualityPost): LiveData<Resource<GenericReqAndResp<VegaGhanaQualityPostResponse>>>
    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun saveVegaQualityWBDetails(wbid: String, batchno: String)
    suspend fun saveVegaQualityWeightWBDetails(wbid: String, batchno: String, weight: String)
    suspend fun savePostLotDetails(qualityParameter: List<VegaGhanaMtnrQualityLot>)
    suspend fun saveMtnrQualityData(qualityParameter: VegaGhanaQuality, batchNo: String)
    suspend fun saveOfflineQualityData(qualityParameter: List<VegaQuality>, batchNo: String, wbid: String)
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>
    suspend fun getGhanaCashewWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>>
    suspend fun updateDeletedItem(weighBridgeId: String)
    suspend fun deleteWBDetals()
    suspend fun updateWBDB(wbid: String, charg: String, message: String, status: Int)
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun getThirdPartyMaterials(): List<VegaCoffeeThirdPartyMaterialDetail>

    suspend fun getDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<Resource<GenericReqAndResp<BatchNumResponse>>>

    suspend fun getOfflineDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<VegaGhanaQualityMtnBatch>

    //== Start MTNR ==
//    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getLotDetailOnline(weighBridgeId: String): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeLot>>>>
    suspend fun getLotQualityDetails(weighBridgeId: String): LiveData<VegaGhanaMtnrQualityLot>
    suspend fun getAllLotQualityDetails(): LiveData<List<VegaGhanaMtnrQualityLot>>
//    suspend fun getQualityParams(
//        materialId: String,
//        valueExist: Boolean?,
//        wbId: String?
//    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(paramPost: VegaGhanaQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>>

    //    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
//    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
//    suspend fun getQualityOfflineList(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getOfflinePreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<List<VegaGhanaLotQualityDetails>>

    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>
    // == End MTNR ==
}

class VegaGhanaQualityRepositoryImpl(private val api: VegaGhanaQualityApi, private val dao: VegaQualityDao) : VegaGhanaQualityRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>> {
        val receiveItem = dao.getReceivingQualityDetail()
        receiveItem.forEach {
            if (dao.isWBExist(it.tmpWbId).isNotEmpty()) return@forEach
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

    override suspend fun getMtnrQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getMtnrQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
    }

    override suspend fun getOfflineSavedQuality(materialId: String, wbId: String?)
            : LiveData<List<VegaQualityWithQualitative>> =
        dao.getOfflineSavedQuality(materialId, wbId)

    override suspend fun postQuality(qualityPost: VegaGhanaQualityPost): LiveData<Resource<GenericReqAndResp<VegaGhanaQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaGhanaQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun getQualityOfflineList() = dao.getGhanaQualityOfflineList("PROCURE")

    //    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()
    override suspend fun getQualityMtnrOfflineList() = dao.getGhanaQualityOfflineList("STO")
    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun saveVegaQualityWBDetails(wbid: String, batchNo: String) =
        dao.saveVegaQualityWBDetails(wbid, batchNo)

    override suspend fun saveVegaQualityWeightWBDetails(wbid: String, batchNo: String, weight: String) =
        dao.saveVegaQualityWeightWBDetails(wbid, batchNo, weight)

    override suspend fun savePostLotDetails(qualityParameter: List<VegaGhanaMtnrQualityLot>) =
        dao.savePostLotDetails(qualityParameter)

    override suspend fun saveMtnrQualityData(qualityParameter: VegaGhanaQuality, batchNo: String) =
        dao.saveMtnrQualityData(qualityParameter, batchNo)

    override suspend fun saveOfflineQualityData(qualityParameter: List<VegaQuality>, batchNo: String, wbid: String) =
        dao.insertOfflineQuality(qualityParameter)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)
    override suspend fun getWBWithQuality(weighBridgeID: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        return if (weighBridgeID.isNotEmpty()) dao.getWBWithQuality(weighBridgeID) else dao.getWBWithQuality()
    }

    override suspend fun getGhanaCashewWBWithQuality(weighBridgeType: String): LiveData<List<VegaWeighBridgeWithQualityParams>> {
        return dao.getGhanaCashewWBWithQuality(weighBridgeType)
    }

    override suspend fun updateDeletedItem(weighBridgeId: String) = dao.updateDeletedItem(weighBridgeId)
    override suspend fun deleteWBDetals() = dao.deleteWBDetals("STO")
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

    override suspend fun getLotQualityDetails(weighBridgeId: String): LiveData<VegaGhanaMtnrQualityLot> =
        dao.getLotQualityDetails(weighBridgeId)

    override suspend fun getAllLotQualityDetails(): LiveData<List<VegaGhanaMtnrQualityLot>> =
        dao.getAllLotQualityDetails()

//    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
//            : LiveData<List<VegaQualityParamsWithQualitative>> {
//        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
//            dao.getQualityParameter(materialId)
//    }

    override suspend fun postQuality(paramPost: VegaGhanaQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaGhanaQualityParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGhanaQualityParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaGhanaQualityParamPost> =
                api.postQuality(paramPost)
        }.build().asLiveData()
    }

    override suspend fun getDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<BatchNumResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<BatchNumResponse> =
                api.getDeliveryBatchNumber(currentKey, deliveryNo, posnr)
        }.build().asLiveData()
    }

    override suspend fun getOfflineDeliveryBatchNumber(deliveryNo: String, posnr: String) =
        dao.getOfflineDeliveryBatchNumber(deliveryNo)


//    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) = dao.saveQualityData(qualityParameter, batchNo)

//    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = dao.insertWeighBridge(weighBridge)

//    override suspend fun getQualityOfflineList() = dao.getQualityOfflineList()

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getOfflinePreSamplingQualityList(batchNo: String, materialId: String) =
        dao.getOfflinePreSamplingQualityList(batchNo, materialId)

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
