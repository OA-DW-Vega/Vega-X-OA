package com.olam.warehouse.vegax.exportsalescoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.VegaCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalescoffee.data.repo.VegaCoffeeExportSalesRepository
import java.util.*

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCoffeeExportSalesUsecase(val repo: VegaCoffeeExportSalesRepository) {

    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getOTWithContainer(otNumber: String) = repo.getOTWithContainer(otNumber)
    suspend fun saveContainer(container: VegaCoffeeExportSalesContainer) = repo.saveContainer(container)
    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun getQualityParams(charge: String, materialList: List<String>, whId: String) =
        repo.getQualityParams(charge, materialList, whId)

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots = repo.validateLot(batchNumber)
    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) =
        repo.saveLotDetails(vegaCoffeeSalesLots)

    suspend fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots> =
        repo.getContainerWithLots(containerNumber)

    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots) = repo.removeLotDetails(lot)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer =
        repo.validateContainer(containerNumber)

    suspend fun getAddedLotList() = repo.getAddedLotList()
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeExportSalesPostRequest) =
        repo.postDeliveryDetail(vegaDeliveryPost)

    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>) = repo.updateLotEditWeight(lot)
    suspend fun removeConatinerWitfLots(containerNumber: String) = repo.removeConatinerWitfLots(containerNumber)
    suspend fun updateContainerId(oldContainerId: String, newContainerId: String) =
        repo.updateContainerId(oldContainerId, newContainerId)
}
