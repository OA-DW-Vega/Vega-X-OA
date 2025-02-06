package com.olam.warehouse.vegax.localsalescameroon.data.domain.usecase

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesPostRequest
import com.olam.warehouse.vegax.localsalescameroon.data.repo.VegaCoffeeSalesRepository
import java.util.*


class VegaCameroonSalesDispatchUseCase(private val repo: VegaCoffeeSalesRepository) {
    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getDispatchSalesItem(soNumber: String, salesType: String, salesTempId: String) =
        repo.getDispatchSalesItem(soNumber, salesType, salesTempId)

    suspend fun getDispatchSummarySalesItem(
        soNumber: String,
        salesType: String,
        salesTempId: String
    ) =
        repo.getDispatchSummarySalesItem(soNumber, salesType, salesTempId)

    suspend fun getCustomLocations() = repo.getCustomLocations()

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun saveDispatchAndLots(
        salesOrder: VegaCoffeeSalesOrder,
        lots: ArrayList<VegaCoffeeSalesLots>
    ) =
        repo.saveDispatchAndLots(salesOrder, lots)

    suspend fun validateLot(batchNumber: String): VegaCoffeeSalesLots =
        repo.validateLot(batchNumber)

    suspend fun getQualityParams(charge: String, materialList: List<String>, whId: String) =
        repo.getQualityParams(charge, materialList, whId)

    suspend fun removeLotDetails(lot: VegaCoffeeSalesLots) = repo.removeLotDetails(lot)
    suspend fun getBagItems(batchNumber: String, materialCode: String, salesTempId: String) = repo.getBagItems(batchNumber, materialCode, salesTempId)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repo.getPalletDetails(batchNumber, material)

    suspend fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = repo.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = repo.deleteBagDetails(bagMaterial)
    suspend fun getPendingListLot(type: String) = repo.getPendingLotList(type)
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeSalesLots>) = repo.updateLotEditWeight(lot)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCameroonSalesPostRequest) =
        repo.postDeliveryDetail(vegaDeliveryPost)

    suspend fun deleteTempData(salesTempId: String) = repo.deleteTempData(salesTempId)
    suspend fun getStorageLocations() = repo.getStoragetLocations()

    suspend fun getGrades(materialCode: String) = repo.getGrades(materialCode)
    suspend fun getCertification(materialCode: String) = repo.getCertification(materialCode)
    suspend fun getMaterialQualityGrades(materialCode: String) = repo.getMaterialQualityGrades(materialCode)
    suspend fun getOfflineSalesPalletBags(batchNumber: String) = repo.getOfflineSalesPalletBags(batchNumber)
    suspend fun saveSalesLot(salesLots: VegaCoffeeSalesLots)= repo.saveSalesLot(salesLots)
    suspend fun getAllProducts() = repo.getAllProduct()

}
