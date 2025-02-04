package com.olam.warehouse.vegax.gateentry.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaGateEntryDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.gateentry.data.api.VegaGateEntryApi
import com.olam.warehouse.vegax.gateentry.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentry.data.domain.model.VegaGateEntryResponse

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryRepository {
    suspend fun getWaitingTrucks(): LiveData<Resource<List<VegaGateEntry>>>
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
}

class VegaGateEntryRepositoryImpl(
    private val api: VegaGateEntryApi,
    private val dao: VegaGateEntryDao,
    private val masterDao: MasterDao
) : VegaGateEntryRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWaitingTrucks(): LiveData<Resource<List<VegaGateEntry>>> {

        return object : NetworkBoundResource<List<VegaGateEntry>, GenericReqAndResp<List<VegaGateEntry>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaGateEntry>>): List<VegaGateEntry> =
                response.data

            override suspend fun saveCallResults(items: List<VegaGateEntry>) = dao.save(items)
            override fun shouldFetch(data: List<VegaGateEntry>?): Boolean = true
            override suspend fun loadFromDb(): List<VegaGateEntry> = dao.getWaitingTruckListDetails()
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                api.fetchWaitingTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun postGateEntryData(gateEntryPost: VegaGateEntryPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGateEntryResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGateEntryResponse> =
                api.postGateEntryData(gateEntryPost)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSupplierZone(bcApprover: String) = dao.getSupplierZone(bcApprover)
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

}
