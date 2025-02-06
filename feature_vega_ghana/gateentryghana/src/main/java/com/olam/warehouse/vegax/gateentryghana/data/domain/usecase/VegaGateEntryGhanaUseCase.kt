package com.olam.warehouse.vegax.gateentryghana.data.domain.usecase

import com.olam.warehouse.vegax.gateentryghana.data.domain.model.VegaGateEntryGhanaPost
import com.olam.warehouse.vegax.gateentryghana.data.repo.VegaGateEntryGhanaRepository

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
class VegaGateEntryGhanaUseCase(private val repository: VegaGateEntryGhanaRepository) {

    suspend fun getWaitingTrucks() = repository.getWaitingTrucks()
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryGhanaPost) = repository.postGateEntryData(gateEntryPost)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()

}
