package com.olam.warehouse.vegax.mtntcoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.vegax.mtntcoffee.data.domain.model.VegaCoffeeDeliveryPost
import com.olam.warehouse.vegax.mtntcoffee.data.repo.VegaCoffeeMtntRepository


class VegaCoffeeDispatchUseCase(private val repository: VegaCoffeeMtntRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getPurchaseOrder(receivingWerks: String) = repository.getPurchaseOrder(receivingWerks)
    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun deleteTruckAndLots(whId: String) = repository.deleteTruckAndLots(whId)
    suspend fun deleteMaterialData(whId: String) = repository.deleteMaterialData(whId)
    suspend fun getProducts(code: String) = repository.getSingleProduct(code)
    suspend fun getAllProducts() = repository.getAllProduct()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeDeliveryPost) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun getDelivery(delivery: String, deliveryItem: String) = repository.getDelivery(delivery, deliveryItem)
    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaCoffeeDeliveryPost) =
        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)
    suspend fun insertTruckInfo(dispatch: VegaCocoaDispatchWB) = repository.insertTruckInfo(dispatch)
    suspend fun insertMaterialDetails(list: List<VegaCoffeePurchaseOrderMaterialModel>) =
        repository.insertMaterialInfo(list)
    suspend fun updateStartLoad(startTime: String, whId: String) = repository.updateStartLoadTime(startTime, whId)
    suspend fun updateEndTime(endTime: String, wholeTime: String, whId: String) =
        repository.updateEndLoadTime(endTime, wholeTime, whId)

    suspend fun updateLotWeight(weight: String, batchNumber: String) = repository.updateLot(batchNumber, weight)
    suspend fun removeLotFromTruck(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun updateRemark(remark: String, isStart: Boolean, batchNumber: String) =
        repository.updateRemarks(remark, isStart, batchNumber)

    suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String) =
        repository.updateDeliveryItem(deliveryItem, deliveryStatus, whId)

    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaCocoaDispatchLots>
    ) = repository.saveDispatchAndLots(dispatchData, dispatchLotsList)

    suspend fun getTruckDetails(whId: String): LiveData<VegaCocoaDispatchWB> = repository.getWbInfo(whId)

    suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>> = repository.getLotsDetails(whId)

    suspend fun validateLot(batchNumber: String): VegaCocoaDispatchLots = repository.validateLot(batchNumber)

    suspend fun insertLot(whId: String, lot: VegaCocoaDispatchLots) = repository.insertLot(whId, lot)
    suspend fun insertLotList(whId: String, lot: List<VegaCocoaDispatchLots>) = repository.insertLotList(whId, lot)
    suspend fun getStocks(material: ArrayList<String>) = repository.getStocksByMaterial(material)
    suspend fun getMtntWithLots(wbId: String) = repository.getMtntWithLots(wbId)
    suspend fun getMtntWithLotsAndMaterial(wbId: String) = repository.getMtntWithLotsAndMaterial(wbId)
    suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNumber: String) =
        repository.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNumber)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.saveBagDetails(bagMaterial)

    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun getBagItems(batchNumber: String, material: String) = repository.getBagItems(batchNumber, material)
    suspend fun getAllBagItems() = repository.getAllBagItems()
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getWeighScaleInfo(whId: String, stoNumber: String) = repository.getWeighScaleInfo(whId, stoNumber)
    suspend fun updateAllSyncStatus(model: VegaCocoaMtntWithLots) =
        repository.updateAllSync(model)
}
