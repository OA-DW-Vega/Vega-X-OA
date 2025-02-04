package com.olam.warehouse.vegax.mtntghana.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaGhanaPurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.model.VegaGhanaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMergedDeliveryPostResponse
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntDeliveryPost
import com.olam.warehouse.vegax.mtntghana.data.domain.model.VegaGhanaMtntMergedDeliveryPost
import com.olam.warehouse.vegax.mtntghana.data.repo.VegaGhanaMtntRepository


class VegaGhanaDispatchUseCase(private val repository: VegaGhanaMtntRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getPurchaseOrder(receivingWerks: String) = repository.getPurchaseOrder(receivingWerks)
    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun deleteTruckAndLots(whId: String) = repository.deleteTruckAndLots(whId)
    suspend fun deleteMaterialData(whId: String) = repository.deleteMaterialData(whId)
    suspend fun deleteMaterialData() = repository.deleteMaterialData()
    suspend fun getProducts(code: String) = repository.getSingleProduct(code)
    suspend fun getAllProducts() = repository.getAllProduct()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntDeliveryPost) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun saveLot(
        list: VegaGhanaCocoaDispatchLots
    ) =
        repository.saveLots(list)

    suspend fun saveLotData(
        list: VegaGhanaCocoaDispatchLots
    ) =
        repository.saveLotData(list)

    suspend fun getDelivery(delivery: String, deliveryItem: String) = repository.getDelivery(delivery, deliveryItem)
//    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaNigeriaSesameMtntDeliveryPost) =
//        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntMergedDeliveryPost) =
        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaGhanaMtntDeliveryPost) =
        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

    suspend fun postWs(vegaSesameDeliveryPost: VegaGhanaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaGhanaMergedDeliveryPostResponse>>> =
        repository.postWs(vegaSesameDeliveryPost)

    suspend fun insertTruckInfo(dispatch: VegaCocoaDispatchWB) = repository.insertTruckInfo(dispatch)
    suspend fun insertMaterialDetails(list: List<VegaGhanaPurchaseOrderMaterialModel>) =
        repository.insertMaterialInfo(list)

    suspend fun updateStartLoad(startTime: String, whId: String) = repository.updateStartLoadTime(startTime, whId)
    suspend fun updateEndTime(endTime: String, wholeTime: String, whId: String) =
        repository.updateEndLoadTime(endTime, wholeTime, whId)

    suspend fun updateLotWeight(weight: String, batchNumber: String) = repository.updateLot(batchNumber, weight)
    suspend fun removeLotFromTruck(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun removeGhanaLotFromList(batchNumber: String,wbid: String) = repository.removeGhanaLotFromList(batchNumber,wbid)
    suspend fun removeLotFromList() = repository.removeLotList()
    suspend fun removeGhanaLotList(wbid: String) = repository.removeGhanaLotList(wbid)
    suspend fun updateRemark(remark: String, isStart: Boolean, batchNumber: String) =
        repository.updateRemarks(remark, isStart, batchNumber)

    suspend fun updateDeliveryItem(deliveryItem: String, deliveryStatus: Boolean, whId: String) =
        repository.updateDeliveryItem(deliveryItem, deliveryStatus, whId)

    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaGhanaCocoaDispatchLots>
    ) = repository.saveDispatchAndLots(dispatchData, dispatchLotsList)

    suspend fun getTruckDetails(whId: String): LiveData<VegaCocoaDispatchWB> = repository.getWbInfo(whId)
    suspend fun getTruckDetails() = repository.getGhanaWbInfo()

    suspend fun getLotsDetails(whId: String): LiveData<List<VegaGhanaCocoaDispatchLots>> =
        repository.getLotsDetails(whId)

    suspend fun validateLot(batchNumber: String): VegaGhanaCocoaDispatchLots = repository.validateLot(batchNumber)

    suspend fun insertLot(whId: String, lot: VegaGhanaCocoaDispatchLots) = repository.insertLot(whId, lot)
    suspend fun insertLotList(whId: String, lot: List<VegaGhanaCocoaDispatchLots>) = repository.insertLotList(whId, lot)
    suspend fun getStocks(material: ArrayList<String>) = repository.getStocksByMaterial(material)
    suspend fun getDispatchLotList(wbid: String) = repository.getDispatchLotList(wbid)
    suspend fun getMtntWithLots(wbId: String) = repository.getMtntWithLots(wbId)
    suspend fun getMtntWithLotsAndMaterial(wbId: String) = repository.getMtntWithLotsAndMaterial(wbId)
    suspend fun getOfflineGradeWithBagsRmin(fgrnIdWithMatrial: String, batchNumber: String) =
        repository.getOfflineGradeWithBagsRmin(fgrnIdWithMatrial, batchNumber)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.saveBagDetails(bagMaterial)

    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun deleteBagDetails() = repository.deleteBagDetails()
    suspend fun deleteBag(batchNumber: String) = repository.deleteBag(batchNumber)
    suspend fun getBagItems(batchNumber: String, material: String) = repository.getBagItems(batchNumber, material)
    suspend fun getBagItems(batchNumber: String, material: String, wbid: String) =
        repository.getBagItems(batchNumber, material, wbid)

    suspend fun getAllBagItems() = repository.getAllBagItems()
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getWeighScaleInfo(whId: String, stoNumber: String) = repository.getWeighScaleInfo(whId, stoNumber)
    suspend fun getWeighScaleInfo() = repository.getWeighScaleInfo()
    suspend fun updateAllSyncStatus(model: VegaGhanaCocoaMtntWithLots) =
        repository.updateAllSync(model)

    suspend fun updateSyncStatus(model: VegaGhanaCocoaMtntWithLots) =
        repository.updateSync(model)

    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun getPurchaseOrderOffline(): LiveData<List<VegaCocoaPurchaseOrders>> =
        repository.getPurchaseOrderOffline()

    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>> = repository.getStockListOffline()
    suspend fun updateStockDetails(weight: String, batchNo: String) =
        repository.updateStockDetails(weight, batchNo)

    suspend fun updateMtntPurchaseOrderDetails(weight: String, po: String) =
        repository.updateMtntPurchaseOrderDetails(weight, po)

    suspend fun updateSyncedMtntDeletedItem(status: Int) = repository.updateSyncedMtntDeletedItem(status)

}
