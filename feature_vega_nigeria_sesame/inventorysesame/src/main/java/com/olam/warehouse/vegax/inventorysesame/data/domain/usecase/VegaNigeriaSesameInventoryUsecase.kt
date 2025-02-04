package com.olam.warehouse.vegax.inventorysesame.data.domain.usecase

import com.olam.warehouse.vegax.inventorysesame.data.repo.VegaNigeriaSesameInventoryRepository

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaNigeriaSesamenventoryUseCase(private val repository: VegaNigeriaSesameInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)
}
