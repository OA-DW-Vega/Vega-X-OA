package com.olam.warehouse.vegax.inventoryecuador.data.domain.usecase

import com.olam.warehouse.vegax.inventoryecuador.data.repo.VegaEcuadorInventoryRepository

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaEcuadorInventoryUseCase(private val repository: VegaEcuadorInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
}
