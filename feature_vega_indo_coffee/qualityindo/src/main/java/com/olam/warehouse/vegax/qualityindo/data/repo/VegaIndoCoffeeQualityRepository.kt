package com.olam.warehouse.vegax.qualityindo.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeQualityDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.qualityindo.data.api.VegaIndoCoffeeQualityApi
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualityindo.utils.PROCURE
import com.olam.warehouse.vegax.qualityindo.utils.prepareData
import com.olam.warehouse.vegax.qualityindo.utils.prepareWeighBridgeData

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
interface VegaIndoCoffeeQualityRepository {
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

    suspend fun postQuality(paramPost: VegaIndoCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>>
    suspend fun postQualitySupplier(paramPost: VegaIndoCoffeeQualitySupplierParamPost): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>>
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

    suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>>
    suspend fun getLotDetailOfflineLocal(tempWbId: String): LiveData<List<VegaCoffeeReceiveLots>>
    suspend fun saveQualityLot(lot: List<VegaCoffeeLot>)
    suspend fun deleteAllItem(tmpWbId: String)
    suspend fun getLotDetailOfflineQtyLocal(tempWbId: String): LiveData<List<VegaCoffeeLot>>

    suspend fun getVegaMaterials(): LiveData<List<VegaMaterial>>

}

class VegaIndoCoffeeQualityRepositoryImpl(
    private val api: VegaIndoCoffeeQualityApi,
    private val dao: VegaCoffeeQualityDao
) :
    VegaIndoCoffeeQualityRepository {
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

    override suspend fun postQuality(paramPost: VegaIndoCoffeeQualityParamPost): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeeQualityParamPost> =
                api.postQuality(paramPost)
        }.build().asLiveData()
    }

    override suspend fun postQualitySupplier(paramPost: VegaIndoCoffeeQualitySupplierParamPost): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost> =
                api.postQualitySupplier(paramPost)
        }.build().asLiveData()
    }

    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) {
        dao.insertWeighBridge(weighBridge)
        if (weighBridge.weighBridgeType == PROCURE)
            dao.updateBatchToGrn(weighBridge.weighBridgeId, weighBridge.batchNumber.toString())
    }

    override suspend fun getQualityOfflineList() = dao.getQualityOfflineListIndo()

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

    override suspend fun getWeighBridgeDetail(): LiveData<List<VegaQualityWBDetails>> {
        val receiveItem = dao.getOffloadingDetail()
        receiveItem.forEachIndexed { index, it ->
            if (dao.isWBExist(it.tempWBId.toString()).isNotEmpty()) return@forEachIndexed
            it.item = index.inc().toString()
            dao.insertQualityWbDetail(prepareWeighBridgeData(it))
        }
        return dao.getQualityWeighBridgeDetail()
    }

    override suspend fun getLotDetailOfflineLocal(tempWbId: String): LiveData<List<VegaCoffeeReceiveLots>> {
        return dao.getLotDetailOfflineLocal(tempWbId)
    }

    override suspend fun saveQualityLot(lot: List<VegaCoffeeLot>) = dao.saveQualityLot(lot)
    override suspend fun deleteAllItem(tmpWbId: String) {
        dao.updatedeleteStatus(tmpWbId)
        dao.deleteOfflineParams(tmpWbId)
        dao.deleteOfflineQualityLot(tmpWbId)
        dao.deleteWBItem(tmpWbId)
    }

    override suspend fun getLotDetailOfflineQtyLocal(tempWbId: String) = dao.getLotDetailOfflineQtyLocal(tempWbId)

    override suspend fun getVegaMaterials() = dao.getVegaMaterials()

}
