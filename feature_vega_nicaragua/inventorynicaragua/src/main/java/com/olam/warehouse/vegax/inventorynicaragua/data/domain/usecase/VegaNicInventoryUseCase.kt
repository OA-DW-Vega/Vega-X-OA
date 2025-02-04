package com.olam.warehouse.vegax.inventorynicaragua.data.domain.usecase

import com.olam.warehouse.vegax.inventorynicaragua.data.repo.VegaNicInventoryRepository

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
class VegaNicInventoryUseCase(val repo: VegaNicInventoryRepository) {
    suspend fun getCoffeeInventoryList() = repo.getInventoryDetails()
    suspend fun getCoffeeProducts() = repo.getProducts()
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repo.getQualityParams(charge, material, whId)
    /*suspend fun getCoffeeInventoryList() = repo.getInventoryDetails()
    suspend fun getCoffeeProducts() = repo.getProducts()
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repo.getQualityParams(charge, material, whId)*/
}
