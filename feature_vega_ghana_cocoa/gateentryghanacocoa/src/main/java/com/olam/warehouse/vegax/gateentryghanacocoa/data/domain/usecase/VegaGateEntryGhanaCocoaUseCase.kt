package com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaPost
import com.olam.warehouse.vegax.gateentryghanacocoa.data.repo.VegaGateEntryGhanaCocoaRepository

class VegaGateEntryGhanaCocoaUseCase(private val repository: VegaGateEntryGhanaCocoaRepository) {

    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
    suspend fun postGateEntryData(gateEntryCocoaPost: VegaGateEntryGhanaCocoaPost) =
        repository.postGateEntryData(gateEntryCocoaPost)

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getTruckMangeDetails(seasonId: String) = repository.getTruckDetails(seasonId)
    suspend fun getSeasonDetails() = repository.getSeasonDetails()
    suspend fun getSeasonDetailsOffline() = repository.getSeasonDetailsOffline()
    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getSDWaybillNumber(wbid: String) = repository.getSDWaybillNumber(wbid)
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>
    ) = repository.getfetchWBListforMultiPlants(isMTNT, startDate, endDate, plantList)

}
