package com.olam.warehouse.vegax.gateentrycoffee.data.repo

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
import com.olam.warehouse.vegax.gateentrycoffee.data.api.VegaCoffeeGateEntryApi
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.model.VegaGateEntryResponse


interface VegaCoffeeGateEntryRepository {
    suspend fun getWaitingTrucks(isWeighscale: Boolean): LiveData<Resource<List<VegaGateEntry>>>
    suspend fun postGateEntryData(
        gateEntryPost: VegaGateEntryPost,
        isWB: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaGateEntryResponse>>>

    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun saveGateEntry(gateEntry: VegaGateEntry)
    suspend fun getGateEntryData(commonId: String): LiveData<VegaGateEntry>
    suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
}

class VegaCoffeeGateEntryRepositoryImpl(
    private val api: VegaCoffeeGateEntryApi,
    private val dao: VegaGateEntryDao,
    private val masterDao: MasterDao
) : VegaCoffeeGateEntryRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getWaitingTrucks(isWeighscale: Boolean): LiveData<Resource<List<VegaGateEntry>>> {

        return object : NetworkBoundResource<List<VegaGateEntry>, GenericReqAndResp<List<VegaGateEntry>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaGateEntry>>): List<VegaGateEntry> =
                response.data

            override suspend fun saveCallResults(items: List<VegaGateEntry>) = dao.save(items)
            override fun shouldFetch(data: List<VegaGateEntry>?): Boolean = true
            override suspend fun loadFromDb(): List<VegaGateEntry> = dao.getWaitingTruckListDetails()
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                if (isWeighscale) api.fetchWSWaitingTruckList(currentKey) else api.fetchWaitingTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun postGateEntryData(
        gateEntryPost: VegaGateEntryPost,
        isWB: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaGateEntryResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGateEntryResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGateEntryResponse> =
                if (isWB) api.postGateEntryData(gateEntryPost) else api.postWeighScaleGateEntryData(gateEntryPost)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSupplierZone(bcApprover: String) = dao.getSupplierZone(bcApprover)
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun saveGateEntry(gateEntry: VegaGateEntry) = dao.insertQualityWbDetail(gateEntry)
    override suspend fun getGateEntryData(commonId: String): LiveData<VegaGateEntry> = dao.getGateEntryDetails(commonId)

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                if (isWeighscale) api.getWeighScaleIdDetail(
                    currentKey,
                    wbid
                ) else api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }
}
