package com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.usecase

import com.olam.warehouse.vegax.inventoryghanacocoa.data.repo.VegaGhanaCocoaInventoryRepository

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaGhanaCocoaInventoryUseCase(private val repository: VegaGhanaCocoaInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun fetchWarehouseWithMtns(isInventory: Boolean) =
        repository.fetchWarehouseWithMtns(isInventory)

    suspend fun getProducts() = repository.getProducts()
    suspend fun getuomDetail() = repository.getuomDetail()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)
}
