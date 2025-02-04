package com.olam.warehouse.vegax.offloading.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaOffloadingDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloading.data.api.VegaOffloadingApi
import com.olam.warehouse.vegax.offloading.data.domain.model.BatchNumResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.OffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingLotQuality
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingQualityPost
import com.olam.warehouse.vegax.offloading.utils.prepareData


interface VegaOffloadingRepository {
    suspend fun getTrucks() : LiveData<Resource<List<VegaOffloadingTrucks>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>

    suspend fun postQuality(qualityPost: VegaOffloadingQualityPost): LiveData<Resource<GenericReqAndResp<OffloadingQualityPostResponse>>>
    suspend fun updateDB(wbid: String)
    suspend fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String)
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getSuggestedLocation(
        kor: String,
        origin: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>>
    suspend fun getDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<Resource<GenericReqAndResp<BatchNumResponse>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>>
}

class VegaOffloadingRepositoryImpl(private val api: VegaOffloadingApi,
                                   private val dao: VegaOffloadingDao,
                                   private val masterDao: MasterDao):VegaOffloadingRepository{
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<List<VegaOffloadingTrucks>>> {

        return object : NetworkBoundResource<List<VegaOffloadingTrucks>, GenericReqAndResp<List<VegaOffloadingTrucks>>>() {

            override fun processResponse(response: GenericReqAndResp<List<VegaOffloadingTrucks>>): List<VegaOffloadingTrucks> =
                response.data

            override suspend fun saveCallResults(items: List<VegaOffloadingTrucks>) = dao.save(items)

            override fun shouldFetch(data: List<VegaOffloadingTrucks>?): Boolean = true

            override suspend fun loadFromDb(): List<VegaOffloadingTrucks> = dao.getOffloadingTruckListDetails()

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingTrucks>> =
                api.fetchTruckList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId, "X")) else
            dao.getQualityParameter(materialId, "")
    }

    override suspend fun postQuality(qualityPost: VegaOffloadingQualityPost): LiveData<Resource<GenericReqAndResp<OffloadingQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<OffloadingQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<OffloadingQualityPostResponse> =
                api.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun updateDB(wbid: String) = dao.updateDB(wbid)

    override suspend fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<Resource<GenericReqAndResp<BatchNumResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<BatchNumResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<BatchNumResponse> =
                api.getDeliveryBatchNumber(currentKey, deliveryNo, posnr)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaOffloadingLotQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingLotQuality>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getSuggestedLocation(
        kor: String,
        origin: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCustomStLocation>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCustomStLocation>> =
                api.getSuggestedLocation(kor, origin, materialCode)

        }.build().asLiveData()
    }
}
