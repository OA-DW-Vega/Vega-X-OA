package com.olam.warehouse.vegax.lotqualitynigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.dao.VegaQualityDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.lotqualitynigeria.data.api.VegaCocoaLotQualityApi
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLotDetails
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLots
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityPostResponse
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaLotQualityModel
import com.olam.warehouse.vegax.lotqualitynigeria.utils.prepareData
import java.util.*


interface VegaCocoaLotQualityRepository {

    suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>>
    suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>>
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaLotQualityModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String)
    suspend fun postQuality(qualityPost: VegaQualityPostLot): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>>
    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails)
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun saveInspectionLotDetails(lotDetail: VegaCocoaLotQualityInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaLotPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>
    suspend fun updateDeletedItem(weighBridgeId: String)
}

class VegaCocoaLotQualityRepositoryImpl(
    private val api: VegaCocoaLotQualityApi,
    private val dao: VegaInventoryDao,
    private val daoQ: VegaQualityDao
) : VegaCocoaLotQualityRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaLotQualityModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaLotQualityModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaLotQualityModel> =
                api.getSesameInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun updateDeletedItem(weighBridgeId: String) =
        dao.updateDeletedItem(weighBridgeId)

    override suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaLotPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse> =
                api.postQualityNigeriaPost(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun saveInspectionLotDetails(lotDetail: VegaCocoaLotQualityInspectionLotDetails): LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaCocoaLotQualityPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCocoaLotQualityPostResponse> =
                api.saveInspectionLotDetails(
                    getCurrentKey(),
                    PreferenceHelper.get(Constants.WERKS, ""),
                    lotDetail
                )
        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = daoQ.getCustomLocations()

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(
            daoQ.getQualityParameterWithData(
                materialId,
                wbId
            )
        ) else
            daoQ.getQualityParameter(materialId)
    }

    override suspend fun postQuality(qualityPost: VegaQualityPostLot): LiveData<Resource<GenericReqAndResp<VegaQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) =
        dao.insertWeighBridge(weighBridge)

    override suspend fun getInspectionLots(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>> =
                api.getInspectionLots(getCurrentKey(), PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun getInspectionLotDetails(lotId: String): LiveData<Resource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails> =
                api.getInspectionLotDetails(
                    getCurrentKey(),
                    lotId,
                    PreferenceHelper.get(Constants.WERKS, "")
                )
        }.build().asLiveData()
    }

    override suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

}
