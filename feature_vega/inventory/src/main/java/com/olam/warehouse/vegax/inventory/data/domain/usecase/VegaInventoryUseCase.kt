package com.olam.warehouse.vegax.inventory.data.domain.usecase

import com.olam.warehouse.vegax.inventory.data.repo.VegaInventoryRepository

class VegaInventoryUseCase(private val repository: VegaInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
}
