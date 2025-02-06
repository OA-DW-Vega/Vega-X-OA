package com.olam.warehouse.vegax.inventoryghana.data.domain.usecase

import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryPost
import com.olam.warehouse.vegax.inventoryghana.data.repo.VegaGhanaInventoryRepository

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
class VegaGhanaInventoryUseCase(private val repository: VegaGhanaInventoryRepository) {
    suspend fun getInventoryList() = repository.getInventoryDetails()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getConfigItems(role: String) = repository.getConfigItems(role)
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)
    suspend fun getInventoryReports(paramPost: VegaGhanaInventoryPost) = repository.getInventoryReports(paramPost)

}
