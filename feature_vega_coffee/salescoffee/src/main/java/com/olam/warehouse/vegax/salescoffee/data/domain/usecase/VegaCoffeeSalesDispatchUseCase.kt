package com.olam.warehouse.vegax.salescoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.vegax.salescoffee.data.domain.model.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.salescoffee.data.repo.VegaCoffeeSalesRepository
import java.util.*


class VegaCoffeeSalesDispatchUseCase(private val repo: VegaCoffeeSalesRepository) {
    suspend fun getPurchaseOrder() = repo.getPurchaseOrder()
    suspend fun getWBPurchaseOrder() = repo.getWBPurchaseOrder()
    suspend fun getDispatchSalesItem(soNumber: String, salesType: String, salesTempId: String) =
        repo.getDispatchSalesItem(soNumber, salesType, salesTempId)

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>) =
        repo.saveDispatchAndLots(salesOrder, lots)

    suspend fun validateLot(batchNumber: String): VegaCoffeeSalesLots = repo.validateLot(batchNumber)
    suspend fun getQualityParams(charge: String, materialList: List<String>, whId: String) =
        repo.getQualityParams(charge, materialList, whId)

    suspend fun removeLotDetails(lot: VegaCoffeeSalesLots) = repo.removeLotDetails(lot)
    suspend fun getBagItems(batchNumber: String, materialCode: String) = repo.getBagItems(batchNumber, materialCode)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repo.getPalletDetails(batchNumber, material)

    suspend fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = repo.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = repo.deleteBagDetails(bagMaterial)
    suspend fun getPendingListLot(type: String) = repo.getPendingLotList(type)
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeSalesLots>) = repo.updateLotEditWeight(lot)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest) =
        repo.postDeliveryDetail(vegaDeliveryPost)

    suspend fun deleteTempData(salesTempId: String) = repo.deleteTempData(salesTempId)

    suspend fun getTrucks() = repo.getTrucks()

    suspend fun validateWBLot(batchNumber: String, material: String): VegaCocoaDispatchLots =
        repo.validateWBLot(batchNumber, material)

    suspend fun removeLotFromTruck(batchNumber: String, material: String) = repo.removeLot(batchNumber, material)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repo.getConfigItems(role)

    suspend fun getTruckDetails(whId: String): LiveData<VegaCocoaDispatchWB> = repo.getWbInfo(whId)

    suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>> = repo.getLotsDetails(whId)

    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repo.getQualityParams(charge, material, whId)

    suspend fun getThirdPartyMaterials() = repo.getThirdPartyMaterials()

    suspend fun deleteTruckAndLots(whId: String) = repo.deleteTruckAndLots(whId)

    suspend fun getVendorInfo(vendorId: String): VegaVendor = repo.vendorInfo(vendorId)

    suspend fun getProducts(code: String) = repo.getSingleProduct(code)

    suspend fun insertLot(whId: String, lot: VegaCocoaDispatchLots) = repo.insertLot(whId, lot)

    suspend fun getWeighbridgePurchaseOrder(receivingWerks: String) = repo.getPurchaseOrder(receivingWerks)

    suspend fun updateSuccessData(
        wbId: VegaCocoaDispatchWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    ) =
        repo.updateSuccessStatus(wbId, syncStatus, status, msg, lots)

    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)
}
