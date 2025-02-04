package com.olam.warehouse.vegax.inventoryindo.data.domain.usecase

import com.olam.warehouse.vegax.inventoryindo.data.repo.VegaIndoCoffeeInventoryRepository

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeInventoryUsecase(private val repo: VegaIndoCoffeeInventoryRepository) {
    suspend fun getScanLotDetail(lotId: String) = repo.getScanLotDetail(lotId)
    suspend fun fetchQualityDetails(charge: String, material: String) = repo.fetchQualityDetails(charge, material)
    suspend fun getInventoryList() = repo.getInventoryList()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getConfigItems(role: String) = repo.getConfigItems(role)
}
