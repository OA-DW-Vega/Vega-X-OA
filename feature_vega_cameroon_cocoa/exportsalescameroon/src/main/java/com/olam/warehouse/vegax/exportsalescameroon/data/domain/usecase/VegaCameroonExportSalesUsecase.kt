package com.olam.warehouse.vegax.exportsalescameroon.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonInventoryModel
import com.olam.warehouse.vegax.exportsalescameroon.data.repo.VegaCameroonExportSalesRepository
import java.util.*

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCameroonExportSalesUsecase(val repo: VegaCameroonExportSalesRepository) {

    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getOTWithContainer(otNumber: String) = repo.getOTWithContainer(otNumber)
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonInventoryModel>>> = repo.getContainerInventory(status)
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
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCameroonExportSalesPostRequest) =
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

    suspend fun getStoreLocations() = repo.getStoreLocations()

    suspend fun getExportSalesPalletBags(batchNumber: String) = repo.getExportSalesPalletBags(batchNumber)
   suspend fun saveExportLot(vegaCoffeeSalesLots: VegaCoffeeExportSalesLots) = repo.saveExportSales(vegaCoffeeSalesLots)
   suspend fun getAllProducts() = repo.getAllProduct()
}
