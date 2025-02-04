package com.olam.warehouse.vegax.receivingghanacash.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntPost
import com.olam.warehouse.vegax.receivingghanacash.data.repo.VegaReceivingGhanaMtntRepository

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaReceivingGhanaMtntUseCase(private val repository: VegaReceivingGhanaMtntRepository) {
    suspend fun getWeighBridgeDetail() = repository.getWeighBridgeDetail()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun postMtntData(mtntData: VegaReceivingGhanaMtntPost) = repository.postMtntData(mtntData)
    suspend fun saveMtnt(mtntData: VegaMtnt) = repository.saveMtnt(mtntData)
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        repository.saveMtntLineItems(lineItems)
    suspend fun getPurchaseOrder() = repository.getPurchaseOrder()
    suspend fun getMaterials() = repository.getMaterials()

}
