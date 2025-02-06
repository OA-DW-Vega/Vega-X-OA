package com.olam.warehouse.vegax.gateentrycameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaGateEntryDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.gateentrycameroon.data.api.VegaGateEntryCameroonApi
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonPost
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonResponse

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryCameroonRepository {
//    suspend fun getWaitingTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>
suspend fun getWaitingTrucks1(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryCameroonPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
//    suspend fun getMultiPlantList(): LiveData<List<Plant>>
    suspend fun getFarmerList(): LiveData<List<VegaTrackTraceFarmerData>>

}

class VegaGateEntryCameroonRepositoryImpl(
    private val api: VegaGateEntryCameroonApi,
    private val dao: VegaGateEntryDao,
    private val masterDao: MasterDao
) : VegaGateEntryCameroonRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
/*    override suspend fun getWaitingTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGateEntry>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                api.fetchWaitingTruckList(currentKey)
        }.build().asLiveData()
    }*/

    override suspend fun getWaitingTrucks1(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGateEntry>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                api.fetchWaitingTruckList(currentKey, selectedPlantId)
        }.build().asLiveData()
    }


/*    override suspend fun getWaitingTrucks(): LiveData<Resource<List<VegaGateEntry>>> {

        return object : NetworkBoundResource<List<VegaGateEntry>, GenericReqAndResp<List<VegaGateEntry>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaGateEntry>>): List<VegaGateEntry> =
                response.data

            override suspend fun saveCallResults(items: List<VegaGateEntry>) = dao.save(items)
            override fun shouldFetch(data: List<VegaGateEntry>?): Boolean = true
            override suspend fun loadFromDb(): List<VegaGateEntry> = dao.getWaitingTruckListDetails()
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                api.fetchWaitingTruckList(currentKey)
        }.build().asLiveData()
    }*/

    override suspend fun postGateEntryData(gateEntryPost: VegaGateEntryCameroonPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryCameroonResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGateEntryCameroonResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGateEntryCameroonResponse> =
                api.postGateEntryData(gateEntryPost)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSupplierZone(bcApprover: String) = dao.getSupplierZone(bcApprover)
    override suspend fun getCustomLocations() = dao.getCustomLocations()
//    override suspend fun getMultiPlantList() = dao.getMultiPlantList()


    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getFarmerList() = dao.getFarmerList()


}
