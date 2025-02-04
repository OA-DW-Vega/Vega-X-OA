package com.olam.warehouse.vegax.inventorynigeria.data.domain.usecase

import com.olam.warehouse.vegax.inventorynigeria.data.repo.VegaNigeriaInventoryRepository

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
class VegaNigeriaInventoryUsecase(private val repo: VegaNigeriaInventoryRepository) {
    suspend fun getScanLotDetail(lotId: String) = repo.getScanLotDetail(lotId)
    suspend fun fetchQualityDetails(charge: String, material: String) =
        repo.fetchQualityDetails(charge, material)

    suspend fun getInventoryList() = repo.getInventoryList()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getConfigItems(role: String) = repo.getConfigItems(role)
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repo.getQualityParams(charge, material, whId)
}
