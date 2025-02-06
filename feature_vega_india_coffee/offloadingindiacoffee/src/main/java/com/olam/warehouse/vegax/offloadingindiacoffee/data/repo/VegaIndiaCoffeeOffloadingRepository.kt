package com.olam.warehouse.vegax.offloadingindiacoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaOffloadingDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingindiacoffee.data.api.VegaIndiaCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeBatchNumResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeOffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingLotQuality
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingQualityPost
import com.olam.warehouse.vegax.offloadingindiacoffee.utils.prepareData


interface VegaOffloadingRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>
    suspend fun postQuality(qualityPost: VegaIndiaCoffeeOffloadingQualityPost): LiveData<Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>>
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
    ): LiveData<Resource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>>

    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>>
}

class VegaOffloadingRepositoryImpl(
    private val apiIndiaCoffee: VegaIndiaCoffeeOffloadingApi,
    private val dao: VegaOffloadingDao,
    private val masterDao: MasterDao
) : VegaOffloadingRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaOffloadingTrucks>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaOffloadingTrucks>>>() {

//            override fun processResponse(response: GenericReqAndResp<List<VegaOffloadingTrucks>>): List<VegaOffloadingTrucks> =
//                response.data
//
//            override suspend fun saveCallResults(items: List<VegaOffloadingTrucks>) = dao.save(items)
//
//            override fun shouldFetch(data: List<VegaOffloadingTrucks>?): Boolean = true
//
//            override suspend fun loadFromDb(): List<VegaOffloadingTrucks> = dao.getOffloadingTruckListDetails()

            override suspend fun createCall(): GenericReqAndResp<List<VegaOffloadingTrucks>> =
                apiIndiaCoffee.fetchTruckList(currentKey)

        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getStocksByMaterial(
            materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                    apiIndiaCoffee.getStockList(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId, "X")) else
            dao.getQualityParameterIndiaCoffee(materialId)
    }

    override suspend fun postQuality(qualityPost: VegaIndiaCoffeeOffloadingQualityPost): LiveData<Resource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse> =
                apiIndiaCoffee.postQuality(qualityPost)
        }.build().asLiveData()
    }

    override suspend fun updateDB(wbid: String) = dao.updateDB(wbid)

    override suspend fun saveQualityData(qualityParameter: VegaOffloadingParameter, batchNo: String) =
        dao.saveQualityData(qualityParameter, batchNo)

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getDeliveryBatchNumber(
        deliveryNo: String,
        posnr: String
    ): LiveData<Resource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<IndiaCoffeeBatchNumResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<IndiaCoffeeBatchNumResponse> =
                apiIndiaCoffee.getDeliveryBatchNumber(currentKey, deliveryNo, posnr)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>> =
                apiIndiaCoffee.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getSuggestedLocation(
        kor: String,
        origin: String,
        materialCode: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCustomStLocation>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCustomStLocation>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCustomStLocation>> =
                apiIndiaCoffee.getSuggestedLocation(kor, origin, materialCode)

        }.build().asLiveData()
    }
}
