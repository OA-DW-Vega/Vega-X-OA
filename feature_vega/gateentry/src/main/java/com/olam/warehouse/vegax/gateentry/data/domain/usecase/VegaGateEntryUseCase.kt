package com.olam.warehouse.vegax.gateentry.data.domain.usecase

import com.olam.warehouse.vegax.gateentry.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentry.data.repo.VegaGateEntryRepository

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryUseCase(private val repository: VegaGateEntryRepository) {

    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryPost) = repository.postGateEntryData(gateEntryPost)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()

}
