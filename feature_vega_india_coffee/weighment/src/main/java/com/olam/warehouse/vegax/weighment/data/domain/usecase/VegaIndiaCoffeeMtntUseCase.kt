package com.olam.warehouse.vegax.weighment.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntPost
import com.olam.warehouse.vegax.weighment.data.repo.VegaMtntRepository

class VegaIndiaCoffeeMtntUseCase(private val repository: VegaMtntRepository) {
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun postMtntData(indiaCoffeeMtntData: VegaIndiaCoffeeMtntPost) =
        repository.postMtntData(indiaCoffeeMtntData)

    suspend fun saveMtnt(mtntData: VegaMtnt) = repository.saveMtnt(mtntData)
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        repository.saveMtntLineItems(lineItems)

    suspend fun getPurchaseOrder(plantId: String) = repository.getPurchaseOrder(plantId)
    suspend fun getMaterials() = repository.getMaterials()

}
