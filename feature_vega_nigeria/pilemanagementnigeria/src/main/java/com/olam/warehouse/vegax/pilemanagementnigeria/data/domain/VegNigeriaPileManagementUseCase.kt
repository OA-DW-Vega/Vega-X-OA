package com.olam.warehouse.vegax.pilemanagementnigeria.data.domain

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPilePostRequest
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.VegaNigeriaPileSuccessResponse
import com.olam.warehouse.vegax.pilemanagementnigeria.data.repo.VegaNigeriaPileManagementRepository
import java.util.*

class VegNigeriaPileManagementUseCase(private val repository: VegaNigeriaPileManagementRepository) {

    suspend fun getSuppliers() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun getStockList(materialList: ArrayList<String>) =
        repository.getStockList(materialList)

    suspend fun getStockPile(materialList: ArrayList<String>) =
        repository.getStockPile(materialList)

    suspend fun removeLot(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun insertThirdPartyModel(model: VegaCoffeeThirdPartyRequestModel) =
        repository.insertThirdPartyModel(model)

    suspend fun insertLotList(lot: List<VegaCocoaDispatchLots>) = repository.insertLotList(lot)
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getCreatePile() = repository.getCreatePile()
    suspend fun saveLotInDispatch(
        list: MutableList<VegaCocoaDispatchLots>
    ) =
        repository.saveLots(list)

    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots =
        repository.validateLot(batchNumber)

    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun saveDispatchAndLots(
        salesOrder: VegaCoffeeSalesOrder,
        lots: ArrayList<VegaCoffeeSalesLots>
    ) =
        repository.saveDispatchAndLots(salesOrder, lots)

    suspend fun getPostPile(postPileRequest: VegaNigeriaPilePostRequest): LiveData<Resource<GenericReqAndResp<VegaNigeriaPileSuccessResponse>>> =
        repository.getPostPile(postPileRequest)

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getShiftRemarks(role)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun getPackingMaterial(): LiveData<List<VegaPackageMaterial>> = repository.getPackingMaterial()

}
