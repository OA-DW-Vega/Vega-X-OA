package com.olam.warehouse.vegax.inventorycameroon.data.domain.usecase

import com.olam.warehouse.vegax.inventorycameroon.data.repo.VegaCameroonInventoryRepository

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaCameroonInventoryUseCase(private val repository: VegaCameroonInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
}
