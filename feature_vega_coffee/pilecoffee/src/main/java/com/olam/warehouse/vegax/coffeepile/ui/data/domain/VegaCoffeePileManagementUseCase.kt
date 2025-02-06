package com.olam.warehouse.vegax.coffeepile.ui.data.domain

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaCoffeePilePostRequest
import com.olam.warehouse.vegax.coffeepile.ui.data.domain.model.VegaCoffeePileResponse
import com.olam.warehouse.vegax.coffeepile.ui.data.repo.VegaCoffeePileManagementRepository
import java.util.*

class VegaCoffeePileManagementUseCase(private val repository: VegaCoffeePileManagementRepository) {

    suspend fun getSuppliers() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getStockList(materialList: ArrayList<String>) = repository.getStockList(materialList)
    suspend fun getStockPile(materialList: String) = repository.getStockPile(materialList)
    suspend fun removeLot(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel) = repository.insertThirdPartyModel(model)
    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>) = repository.insertLotList(lot)
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getCreatePile(isPost:Boolean) = repository.getCreatePile(isPost)
    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots = repository.validateLot(batchNumber)
    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>) =
        repository.saveDispatchAndLots(salesOrder, lots)

    suspend fun getPostPile(postPileRequest: VegaCoffeePilePostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeePileResponse>>> = repository.getPostPile(postPileRequest)


}
