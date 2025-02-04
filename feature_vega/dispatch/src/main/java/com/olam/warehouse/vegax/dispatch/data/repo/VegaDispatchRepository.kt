package com.olam.warehouse.vegax.dispatch.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaDispatchDao
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.dispatch.data.api.VegaDispatchApi
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDeliveryPost
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDeliveryPostResponse
import com.olam.warehouse.vegax.dispatch.data.domain.model.VegaDispatchLotQuality

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
interface VegaDispatchRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getStocks(material: String): LiveData<Resource<List<VegaDispatchLots>>>
    suspend fun getStocksOffline(material: String): LiveData<List<VegaDispatchLots>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>
    suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>>

    suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>>

    suspend fun saveDispatchAndLots(dispatchData: VegaDispatchTrucks, dispatchLotsList: MutableList<VegaDispatchLots>)
    suspend fun updateLot(batchNumber: String)
    suspend fun updateDispatchStatus(
        weighBridgeId: String,
        syncStatus: Boolean,
        status: Int,
        msg: String?,
        batch: String
    )
}

class VegaDispatchRepositoryImpl(
    private val api: VegaDispatchApi,
    private val dao: VegaDispatchDao,
    private val masterDao: MasterDao
) : VegaDispatchRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    /*override suspend fun getTrucks(): LiveData<Resource<List<VegaDispatchTrucks>>> {
        return object : NetworkBoundResource<List<VegaDispatchTrucks>, GenericReqAndResp<List<VegaDispatchTrucks>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaDispatchTrucks>>): List<VegaDispatchTrucks> =
                response.data

            override suspend fun saveCallResults(items: List<VegaDispatchTrucks>) = dao.save(items)
            override fun shouldFetch(data: List<VegaDispatchTrucks>?): Boolean = true
            override suspend fun loadFromDb(): List<VegaDispatchTrucks> = dao.getDispatchTruckListDetails()
            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchTrucks>> =
                api.fetchTruckList(currentKey)
        }.build().asLiveData()
    }*/

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaDispatchTrucks>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDispatchTrucks>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchTrucks>> =
                api.fetchTruckList(currentKey, false)
        }.build().asLiveData()
    }

    override suspend fun getCustomLocations() = dao.getCustomLocations()

    /* override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLots>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDispatchLots>>>() {

             override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchLots>> =
                 api.getStocks(currentKey, material)
         }.build().asLiveData()
     }*/

    override suspend fun getStocks(material: String): LiveData<Resource<List<VegaDispatchLots>>> {
        return object : NetworkBoundResource<List<VegaDispatchLots>, GenericReqAndResp<List<VegaDispatchLots>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaDispatchLots>>):
                    List<VegaDispatchLots> = response.data

            override suspend fun saveCallResults(items: List<VegaDispatchLots>) {
                dao.deleteStocks(material)
                dao.saveStock(items)
            }

            override fun shouldFetch(data: List<VegaDispatchLots>?): Boolean = true

            override suspend fun loadFromDb(): List<VegaDispatchLots> = dao.getStocks(material)

            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchLots>> =
                api.getStocks(currentKey, material)

        }.build().asLiveData()
    }

    override suspend fun getStocksOffline(material: String) = dao.getStocksOfflineSingle(material)

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaDispatchLotQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaDispatchLotQuality>> =
                api.getQuality(currentKey, charge, material)

        }.build().asLiveData()
    }

    override suspend fun getDelivery(
        delivery: String,
        deliveryItem: String
    ): LiveData<Resource<GenericReqAndResp<VegaDispatchDelivery>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDispatchDelivery>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDispatchDelivery> =
                api.getDelivery(currentKey, delivery, deliveryItem)
        }.build().asLiveData()
    }

    override suspend fun saveDispatchAndLots(
        dispatchData: VegaDispatchTrucks,
        dispatchLotsList: MutableList<VegaDispatchLots>
    ) {
        dao.insertDispatchTruckDetail(dispatchData)
        dispatchLotsList.forEach {
            it.weighBridgeId = dispatchData.weighBridgeId
            it.isAdded = true
        }
        dao.saveStock(dispatchLotsList)
    }

    override suspend fun updateLot(batchNumber: String) = dao.updateLot(batchNumber)
    override suspend fun updateDispatchStatus(
        weighBridgeId: String,
        syncStatus: Boolean,
        status: Int,
        msg: String?,
        batch: String
    ) =
        dao.updateDispatchStatus(weighBridgeId, syncStatus, status, msg.toString(), batch)
}
