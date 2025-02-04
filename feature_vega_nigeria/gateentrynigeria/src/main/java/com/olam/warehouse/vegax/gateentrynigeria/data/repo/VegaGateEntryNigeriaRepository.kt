package com.olam.warehouse.vegax.gateentrynigeria.data.repo

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
import com.olam.warehouse.vegax.gateentrynigeria.data.api.VegaGateEntryNigeriaApi
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaPost
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaResponse
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaNigeriaGateEntryPostData

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryNigeriaRepository {
    //    suspend fun getWaitingTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>
    suspend fun getWaitingTrucks1(selectedPlantId: String): LiveData<Resource<GenericReqAndResp<List<VegaGateEntry>>>>
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryNigeriaResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun openGrntDetails(
        role: String,
        materialCodes: String,
        vendorCodes: String,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>>
//    suspend fun getMultiPlantList(): LiveData<List<Plant>>
}

class VegaGateEntryNigeriaRepositoryImpl(
    private val api: VegaGateEntryNigeriaApi,
    private val dao: VegaGateEntryDao,
    private val masterDao: MasterDao
) : VegaGateEntryNigeriaRepository {
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

    override suspend fun postGateEntryData(gateEntryPost: VegaGateEntryNigeriaPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryNigeriaResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<VegaGateEntryNigeriaResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGateEntryNigeriaResponse> =
                api.postGateEntryData(gateEntryPost)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSupplierZone(bcApprover: String) = dao.getSupplierZone(bcApprover)
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)
//    override suspend fun getMultiPlantList() = dao.getMultiPlantList()


    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun openGrntDetails(
        role: String,
        materialCodes: String,
        vendorCodes: String,
        plantId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaGateEntryPostData>> =
                api.fetchOpenGrntDetails(role, materialCodes, vendorCodes, plantId)
        }.build().asLiveData()
    }

}
