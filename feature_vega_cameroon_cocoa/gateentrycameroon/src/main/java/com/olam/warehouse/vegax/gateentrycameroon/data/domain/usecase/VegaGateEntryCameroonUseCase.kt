package com.olam.warehouse.vegax.gateentrycameroon.data.domain.usecase

import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonPost
import com.olam.warehouse.vegax.gateentrycameroon.data.repo.VegaGateEntryCameroonRepository

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryCameroonUseCase(private val repository: VegaGateEntryCameroonRepository) {

//    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
suspend fun getWaitingTrucks1(selectedPlantId: String) = repository.getWaitingTrucks1(selectedPlantId)
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryCameroonPost) =
        repository.postGateEntryData(gateEntryPost)

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()
//    suspend fun getMultiPlantList() = repository.getMultiPlantList()

    suspend fun getFarmerList() = repository.getFarmerList()


}
