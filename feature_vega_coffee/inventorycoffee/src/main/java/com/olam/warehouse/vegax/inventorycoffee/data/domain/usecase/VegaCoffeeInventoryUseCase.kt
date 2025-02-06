package com.olam.warehouse.vegax.inventorycoffee.data.domain.usecase

import com.olam.warehouse.vegax.inventorycoffee.data.repo.VegaCoffeeInventoryRepository

class VegaCoffeeInventoryUseCase(val repo: VegaCoffeeInventoryRepository) {
    suspend fun getCoffeeInventoryList() = repo.getInventoryDetails()
    suspend fun getCoffeeProducts() = repo.getProducts()
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repo.getQualityParams(charge, material, whId)
}
