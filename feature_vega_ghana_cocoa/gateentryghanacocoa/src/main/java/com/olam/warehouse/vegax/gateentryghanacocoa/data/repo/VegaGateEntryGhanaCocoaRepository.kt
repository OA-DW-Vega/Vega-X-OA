package com.olam.warehouse.vegax.gateentryghanacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaGateEntryDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCocoaWBMultiPlants
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaghana.entity.VehicleDetails
import com.olam.warehouse.presentation.data.api.TruckManageApi
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.TruckManagementSeasonResponse
import com.olam.warehouse.presentation.data.domain.model.TruckManagementVehicleResponse
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.gateentryghanacocoa.data.api.VegaGateEntryGhanaCocoaApi
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaPost
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaResponse
import com.olam.warehouse.vegax.gateentryghanacocoa.utils.WEIGHSCALE

interface VegaGateEntryGhanaCocoaRepository {
    suspend fun getWaitingTrucks(): LiveData<Resource<List<VegaGateEntry>>>
    suspend fun postGateEntryData(gateEntryCocoaPost: VegaGateEntryGhanaCocoaPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getSupplierZone(bcApprover: String): LiveData<List<VegaBcZoneMapping>>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getTruckDetails(seasonId:String): LiveData<Resource<TruckManagementVehicleResponse>>
    suspend fun getSeasonDetails(): LiveData<Resource<TruckManagementSeasonResponse>>
    suspend fun getSeasonDetailsOffline(): LiveData<List<VehicleDetails>>
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getSDWaybillNumber(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceiving>>>
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>
    suspend fun getfetchWBListforMultiPlants(isMTNT: Boolean,startDate: String,endDate: String,plantList: List<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>>
}

class VegaGateEntryGhanaRepositoryImpl(
    private val cocoaApi: VegaGateEntryGhanaCocoaApi,
    private val apiTruckManage: TruckManageApi,
    private val dao: VegaGateEntryDao,
    private val masterDao: MasterDao
) : VegaGateEntryGhanaCocoaRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getSeasonDetails(): LiveData<Resource<TruckManagementSeasonResponse>> {
        return object : NetworkOnlyBoundResource<TruckManagementSeasonResponse>() {
            override suspend fun createCall(): TruckManagementSeasonResponse =
                apiTruckManage.getSeasonDetails()
        }.build().asLiveData()
    }

    override suspend fun getTruckDetails(seasonId:String): LiveData<Resource<TruckManagementVehicleResponse>> {
        return object : NetworkOnlyBoundResource<TruckManagementVehicleResponse>() {
            override suspend fun createCall(): TruckManagementVehicleResponse =
                apiTruckManage.getTruckDetails(seasonId)
        }.build().asLiveData()
    }

    override suspend fun getWaitingTrucks(): LiveData<Resource<List<VegaGateEntry>>> {

        return object : NetworkBoundResource<List<VegaGateEntry>, GenericReqAndResp<List<VegaGateEntry>>>() {
            override fun processResponse(response: GenericReqAndResp<List<VegaGateEntry>>): List<VegaGateEntry> =
                response.data

            override suspend fun saveCallResults(items: List<VegaGateEntry>) = dao.save(items)
            override fun shouldFetch(data: List<VegaGateEntry>?): Boolean = true
            override suspend fun loadFromDb(): List<VegaGateEntry> = dao.getWaitingTruckListDetails()
            override suspend fun createCall(): GenericReqAndResp<List<VegaGateEntry>> =
                cocoaApi.fetchWaitingTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun postGateEntryData(gateEntryCocoaPost: VegaGateEntryGhanaCocoaPost): LiveData<Resource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaGateEntryGhanaCocoaResponse> =
                cocoaApi.postGateEntryData(gateEntryCocoaPost)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getSeasonDetailsOffline() = dao.getSeasonDetailsOffline()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getSupplierZone(bcApprover: String) = dao.getSupplierZone(bcApprover)
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                cocoaApi.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                cocoaApi.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                cocoaApi.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                cocoaApi.fetchTruckList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaWBMultiPlants>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaWBMultiPlants>> =
                cocoaApi.fetchWBListforMultiPlants(
                    currentKey,
                    isMTNT,
                    startDate,
                    endDate,
                    plantList,
                    WEIGHSCALE
                )
        }.build().asLiveData()
    }

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)

    override suspend fun getSDWaybillNumber(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeReceiving> =
                cocoaApi.fetchSDWaybillNumber(currentKey, wbid)
        }.build().asLiveData()
    }

}
