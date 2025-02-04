package com.olam.warehouse.vegax.exportsalesindo.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesindo.data.repo.VegaIndoCoffeeExportSalesRepository

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeExportSalesUsecase(val repo: VegaIndoCoffeeExportSalesRepository) {

    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getOTWithContainer(tmpId: String) = repo.getOTWithContainer(tmpId)
    suspend fun saveContainer(
        container: VegaCoffeeExportSalesContainer,
        currentSaleOrder: ArrayList<IndoExporSalesMaterialList>
    ) = repo.saveContainer(container, currentSaleOrder)

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun getQualityParams(charge: String, materialList: List<String>, whId: String) =
        repo.getQualityParams(charge, materialList, whId)

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots = repo.validateLot(batchNumber)
    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) =
        repo.saveLotDetails(vegaCoffeeSalesLots)

    suspend fun getContainerWithLots(containerNumber: String): LiveData<IndoContainerWithLots> =
        repo.getContainerWithLots(containerNumber)

    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots) = repo.removeLotDetails(lot)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer =
        repo.validateContainer(containerNumber)

    suspend fun getAddedLotList() = repo.getAddedLotList()
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaIndoCoffeeExportSalesPostRequest) =
        repo.postDeliveryDetail(vegaDeliveryPost)

    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>) = repo.updateLotEditWeight(lot)
    suspend fun removeConatinerWitfLots(containerNumber: String) = repo.removeConatinerWitfLots(containerNumber)
    suspend fun updateContainerId(oldContainerId: String, newContainerId: String) =
        repo.updateContainerId(oldContainerId, newContainerId)

    suspend fun getOfflinePurchaseOrder() = repo.getOfflinePurchaseOrder()
    suspend fun getStockListOffline() = repo.getStockListOffline()
    suspend fun getIndoExportSalesItem() = repo.getIndoExportSalesItem()
    suspend fun deletedItem(tmpId: String) = repo.deletedItem(tmpId)
}
