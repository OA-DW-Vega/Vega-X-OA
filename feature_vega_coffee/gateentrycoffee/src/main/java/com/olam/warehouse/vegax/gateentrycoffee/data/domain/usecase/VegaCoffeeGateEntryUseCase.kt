package com.olam.warehouse.vegax.gateentrycoffee.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentrycoffee.data.repo.VegaCoffeeGateEntryRepository


class VegaCoffeeGateEntryUseCase(private val repository: VegaCoffeeGateEntryRepository) {

    suspend fun getWaitingTrucks(isWeighScale: Boolean) = repository.getWaitingTrucks(isWeighScale)
    suspend fun postGateEntryData(gateEntryPost: VegaGateEntryPost, isWB: Boolean) =
        repository.postGateEntryData(gateEntryPost, isWB)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getSupplierZone(bcApprover: String) = repository.getSupplierZone(bcApprover)
    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun saveGateEntry(gateEntry:VegaGateEntry) = repository.saveGateEntry(gateEntry)
    suspend fun getGateEntryDetails(commonId: String) = repository.getGateEntryData(commonId)
    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighScale: Boolean) =
        repository.getWeighBridgeIdDetail(wbid, isWeighScale)
}
