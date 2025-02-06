package com.olam.warehouse.vegax.inventorycocoa.data.domain.usecase

import com.olam.warehouse.vegax.inventorycocoa.data.repo.VegaCocoaInventoryRepository

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */
class VegaCocoaInventoryUsecase(private val repo: VegaCocoaInventoryRepository) {
    suspend fun getScanLotDetail(lotId: String) = repo.getScanLotDetail(lotId)
    suspend fun fetchQualityDetails(charge: String, material: String) = repo.fetchQualityDetails(charge, material)
    suspend fun getInventoryList() = repo.getInventoryList()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getConfigItems(role: String) = repo.getConfigItems(role)
}
