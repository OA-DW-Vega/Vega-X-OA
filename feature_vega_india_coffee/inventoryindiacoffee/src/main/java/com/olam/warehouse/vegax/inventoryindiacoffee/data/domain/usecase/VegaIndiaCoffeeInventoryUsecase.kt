package com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.usecase

import com.olam.warehouse.vegax.inventoryindiacoffee.data.repo.VegaIndiaCoffeeInventoryRepository


class VegaIndiaCoffeeInventoryUseCase(private val repository: VegaIndiaCoffeeInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)
}
