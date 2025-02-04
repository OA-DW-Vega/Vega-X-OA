package com.olam.warehouse.vegax.receiving.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.vegax.receiving.data.domain.model.VegaMtntPost
import com.olam.warehouse.vegax.receiving.data.repo.VegaMtntRepository

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaMtntUseCase(private val repository: VegaMtntRepository) {
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun postMtntData(mtntData: VegaMtntPost) = repository.postMtntData(mtntData)
    suspend fun saveMtnt(mtntData: VegaMtnt) = repository.saveMtnt(mtntData)
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        repository.saveMtntLineItems(lineItems)

    suspend fun getPurchaseOrder(plantId: String) = repository.getPurchaseOrder(plantId)
    suspend fun getMaterials() = repository.getMaterials()

}
