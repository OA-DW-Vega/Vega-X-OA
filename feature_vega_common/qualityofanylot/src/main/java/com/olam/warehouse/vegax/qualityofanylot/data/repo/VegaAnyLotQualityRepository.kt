package com.olam.warehouse.vegax.qualityofanylot.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
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
import com.olam.warehouse.vegax.qualityofanylot.data.api.VegaAnyLotQualityApi
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListResponse
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostResponse
import com.olam.warehouse.vegax.qualityofanylot.utils.prepareData
import java.util.ArrayList

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

interface VegaAnyLotQualityRepository {
    // suspend fun getQualityDetailsById(key:String,id:String):LiveData
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun getAllStockByPlant():LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun getSavedQualityLotList(plant: String,batchNo: String):LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>
    suspend fun saveOrPostQuality(request: VegaAnyLotQualityPostRequest):LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>
    suspend fun deleteTransaction(id:String):LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>>
    suspend fun getQualityById(key: String,id: String):LiveData<Resource<GenericReqAndResp<VegaAnyLotListData>>>
    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getValidLots(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun postBatchQuality(qualityPost: VegaQualityNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>>


}

class VegaAnyLotQualityRepositoryImpl(
    private val api: VegaAnyLotQualityApi,
    private val dao: VegaInventoryDao,
    private val daoQ: VegaQualityDao
) : VegaAnyLotQualityRepository {


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

    override suspend fun postBatchQuality(qualityPost: VegaQualityNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse> =
                api.postQualityBatchPost(qualityPost)
        }.build().asLiveData()
    }


    override suspend fun getAllStockByPlant(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getAllStockByPlant(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getSavedQualityLotList(
        plant: String,
        batchNo: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaAnyLotListData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaAnyLotListData>> =
                api.getQualityDetails(getCurrentKey(),plant,batchNo)
        }.build().asLiveData()
    }


    override suspend fun saveOrPostQuality(request: VegaAnyLotQualityPostRequest): LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaAnyLotListData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaAnyLotListData>> =
                api.postOrSaveQualityDetails(request)
        }.build().asLiveData()

    }

    override suspend fun deleteTransaction(id: String): LiveData<Resource<GenericReqAndResp<List<VegaAnyLotListData>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaAnyLotListData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaAnyLotListData>> =
                api.deleteTransactionItem(id)
        }.build().asLiveData()
    }



    override suspend fun getQualityById(
        key: String,
        id: String
    ): LiveData<Resource<GenericReqAndResp<VegaAnyLotListData>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaAnyLotListData>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaAnyLotListData> =
                api.getQualityDetailsById(key, id)
        }.build().asLiveData()

    }

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

    override suspend fun getValidLots(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getValidLotId(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }

}
