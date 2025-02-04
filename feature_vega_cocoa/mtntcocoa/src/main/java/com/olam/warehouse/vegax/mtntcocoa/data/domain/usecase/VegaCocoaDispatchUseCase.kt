package com.olam.warehouse.vegax.mtntcocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaVirtualPostRequest
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaWeighscaleDeliveryPost
import com.olam.warehouse.vegax.mtntcocoa.data.repo.VegaCocoaMtntRepository


class VegaCocoaDispatchUseCase(private val repository: VegaCocoaMtntRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getPurchaseOrder(receivingWerks: String) =
        repository.getPurchaseOrder(receivingWerks)

    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun getQualityNoWeighmentParams(charge: String, material: String, whId: String) =
        repository.getQualityNoWeighmentParams(charge, material, whId)

    suspend fun deleteTruckAndLots(whId: String) = repository.deleteTruckAndLots(whId)
    suspend fun getProducts(code: String) = repository.getSingleProduct(code)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)

    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaDeliveryPost) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun insertTruckInfo(dispatch: VegaCocoaNoWeighmentModel) = repository.insertTruckInfo(dispatch)
    suspend fun insertTruckInfoWeighscale(dispatch: VegaCocoaDispatchWB) = repository.insertTruckInfoWeighscale(dispatch)
    suspend fun updateStartLoad(startTime: String, whId: String) = repository.updateStartLoadTime(startTime, whId)
    suspend fun updateEndTime(endTime: String, wholeTime: String, whId: String) =
        repository.updateEndLoadTime(endTime, wholeTime, whId)

    suspend fun updateLotWeight(weight: String, batchNumber: String) = repository.updateLot(batchNumber, weight)
    suspend fun removeLotFromTruck(batchNumber: String,material: String) = repository.removeLot(batchNumber,material)
    suspend fun removeLotFromTruckWs(batchNumber: String) = repository.removeLotWs(batchNumber)
    suspend fun removeNoweighmentLot(batchNumber: String,material: String) = repository.removeNoWeighmentLot(batchNumber,material)
    suspend fun removeNoweighment(wbId: String) = repository.removeNoWeighment(wbId)
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

    suspend fun validateLot(batchNumber: String, material: String): VegaCocoaDispatchLots =
        repository.validateLot(batchNumber, material)

    suspend fun getVendorInfo(vendorId: String): VegaVendor = repository.vendorInfo(vendorId)

    suspend fun insertLot(whId: String, lot: VegaCocoaDispatchLots) = repository.insertLot(whId, lot)

    suspend fun insertLotList(whId: String, lot: List<VegaCocoaDispatchLots>) = repository.insertLotList(whId, lot)

    suspend fun insertLot(whId: String, lot: VegaCocoaNoWeighmentLot) = repository.insertLot(whId, lot)

    suspend fun insertLot(whId: String, lot: List<VegaCocoaNoWeighmentLot>) = repository.insertLot(whId, lot)

    suspend fun getStocks(material: ArrayList<String>) = repository.getStocksByMaterial(material)

    suspend fun getNoWeighmentStocks(material: ArrayList<String>) = repository.getNoWeighmentStocksByMaterial(material)

    suspend fun getSupplier() = repository.getSuppliers()

    suspend fun updateSuccessData(
        wbId: VegaCocoaDispatchWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    ) =
        repository.updateSuccessStatus(wbId, syncStatus, status, msg, lots)

    suspend fun updateVirtualSuccessData(
        wbId: VegaCocoaNoWeighmentModel,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaNoWeighmentLot>
    ) =
        repository.updateVirtualSuccessStatus(wbId, syncStatus, status, msg, lots)

    suspend fun getAllProducts() = repository.getAllProduct()

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.saveBagDetails(bagMaterial)
    suspend fun saveBagDetails(bagMaterial: ArrayList<VegaCocoaSweepingBagMaterial>) = repository.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String) =
        repository.deleteBagDetails(batchNumber, mtnNumber)

    suspend fun getBagItems(isweighscale:Boolean,batchNumber: String, material: String) = repository.getBagItems(isweighscale,batchNumber, material)
    suspend fun getAllBagItems() = repository.getAllBagItems()
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getPalletDetailsWs(batchNumber: String, material: String) =
        repository.getPalletDetailsWs(batchNumber, material)

    suspend fun getWeighScaleInfo(whId: String, stoNumber: String, purchase: String) =
        repository.getWeighScaleInfo(whId, stoNumber, purchase)

    suspend fun getWeighScaleInfo(whId: String) = repository.getWeighScaleInfo(whId)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun virtualDeliveryDetail(vegaDeliveryPost: VegaCocoaVirtualPostRequest) =
        repository.virtualDeliveryDetail(vegaDeliveryPost)

    suspend fun getTransaction() =
        repository.getTransactionData()

    suspend fun getPendingList() =
        repository.getPendingList()

    suspend fun getPendingListWithLot() =
        repository.getPendingListWithLots()

    suspend fun getMtntWithLotsAndMaterial(wbId: String) = repository.getMtntWithLotsAndMaterial(wbId)

    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getOfflinePoList() = repository.getOfflinePurchaseOrderList()
    suspend fun getOfflineStockList(material: String) = repository.getOfflineStockList(material)
    suspend fun getOfflineStockInfo(batchNo: String, material: String) =
        repository.getOfflineStockInfo(batchNo, material)
    suspend fun deleteMaterialData(whId: String) = repository.deleteMaterialData(whId)
    suspend fun getWeighScaleInfo(whId: String, stoNumber: String) = repository.getWeighScaleInfo(whId, stoNumber)
    suspend fun insertMaterialDetails(list: List<VegaCoffeePurchaseOrderMaterialModel>) =
        repository.insertMaterialInfo(list)

    suspend fun getMtntWithLots(wbId: String) = repository.getMtntWithLots(wbId)

    suspend fun validateLotWs(batchNumber: String): VegaCocoaDispatchLots = repository.validateLotWs(batchNumber)

    suspend fun updateAllSyncStatus(model: VegaCocoaMtntWithLots) =
        repository.updateAllSync(model)

    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaCocoaWeighscaleDeliveryPost) =
        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

}
