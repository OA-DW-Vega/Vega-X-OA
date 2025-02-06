package com.olam.warehouse.vegax.exportsalesnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.VegaNigeriaInventoryModel
import com.olam.warehouse.vegax.exportsalesnigeria.data.repo.VegaNigeriaExportSalesRepository
import java.util.*

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaNigeriaExportSalesUsecase(val repo: VegaNigeriaExportSalesRepository) {

    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getOTWithContainer(otNumber: String) = repo.getOTWithContainer(otNumber)
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaNigeriaInventoryModel>>> = repo.getContainerInventory(status)
    suspend fun saveContainer(container: VegaCoffeeExportSalesContainer) = repo.saveContainer(container)
    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun getQualityParams(charge: String, materialList: List<String>, whId: String) =
        repo.getQualityParams(charge, materialList, whId)
    suspend fun getBagItems(batchNumber: String, material: String) = repo.getBagItems(batchNumber, material)
    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repo.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(id: Int) = repo.deleteBagDetails(id)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repo.getPalletDetails(batchNumber, material)
    suspend fun getCustomLocations() = repo.getCustomLocations()

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots = repo.validateLot(batchNumber)
    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) =
        repo.saveLotDetails(vegaCoffeeSalesLots)

    suspend fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots> =
        repo.getContainerWithLots(containerNumber)

    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots) = repo.removeLotDetails(lot)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer =
        repo.validateContainer(containerNumber)

    suspend fun getAddedLotList() = repo.getAddedLotList()
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaExportSalesPostRequest) =
        repo.postDeliveryDetail(vegaDeliveryPost)

    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>) =
        repo.updateLotEditWeight(lot)

    suspend fun removeConatinerWitfLots(containerNumber: String) =
        repo.removeConatinerWitfLots(containerNumber)

    suspend fun updateContainerStatus(status: String, containerNumber: String) =
        repo.updateContainerStatus(status, containerNumber)

    suspend fun updateContainerId(oldContainerId: String, newContainerId: String) =
        repo.updateContainerId(oldContainerId, newContainerId)

    suspend fun getShiftRemarkItems(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repo.getShiftRemarks(role)

    suspend fun getAllProducts() = repo.getAllProduct()
}
