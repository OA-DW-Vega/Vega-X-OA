package com.olam.warehouse.vegax.dispatchnigeria.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCoffeePurchaseOrderMaterialModel
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.model.VegaCocoaMtntWithLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.data.repo.VegaNigeriaCocoaMtntRepository


class VegaNigeriaCocoaDispatchUseCase(private val repository: VegaNigeriaCocoaMtntRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getPurchaseOrder(receivingWerks: String) =
        repository.getPurchaseOrder(receivingWerks)

    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun deleteTruckAndLots(whId: String) = repository.deleteTruckAndLots(whId)
    suspend fun deleteMaterialData(whId: String) = repository.deleteMaterialData(whId)
    suspend fun deleteMaterialData() = repository.deleteMaterialData()
    suspend fun getProducts(code: String) = repository.getSingleProduct(code)
    suspend fun getAllProducts() = repository.getAllProduct()
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        repository.getConfigItems(role)

    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ) = repository.getQualityParams(charge, material)

    suspend fun saveLot(
        list: VegaCocoaDispatchLots
    ) =
        repository.saveLots(list)

    suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaPost) =
        repository.postNigeriaQuality(qualityPost)

    suspend fun getWaitingTrucks1(selectedPlantId: String) = repository.getWaitingTrucks1(
        selectedPlantId
    )

    suspend fun getDelivery(delivery: String, deliveryItem: String) =
        repository.getDelivery(delivery, deliveryItem)
//    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost) =
//        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

    suspend fun postWeighScaleDeliveryDetail(vegaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost) =
        repository.postWeighScaleDeliveryDetail(vegaDeliveryPost)

    suspend fun postWs(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>>> =
        repository.postWs(vegaCocoaDeliveryPost)

    suspend fun postWsBinMerge(vegaCocoaDeliveryPost: VegaNigeriaCocoaMtntBinMerge): LiveData<Resource<GenericReqAndResp<MergedData>>> =
        repository.postWsBinMerge(vegaCocoaDeliveryPost)

    suspend fun postWeightedAverage(vegaWeightedAvgPost: VegaNigeriaCocoaWeightedAveragePost): LiveData<Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>> =
        repository.postWeightedAverage(vegaWeightedAvgPost)

    suspend fun insertTruckInfo(dispatch: VegaCocoaDispatchWB) =
        repository.insertTruckInfo(dispatch)

    suspend fun insertMaterialDetails(list: List<VegaCoffeePurchaseOrderMaterialModel>) =
        repository.insertMaterialInfo(list)

    suspend fun updateStartLoad(startTime: String, whId: String) =
        repository.updateStartLoadTime(startTime, whId)

    suspend fun updateEndTime(endTime: String, wholeTime: String, whId: String) =
        repository.updateEndLoadTime(endTime, wholeTime, whId)

    suspend fun updateLotWeight(weight: String, batchNumber: String) =
        repository.updateLot(batchNumber, weight)

    suspend fun removeLotFromTruck(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun removeLotFromList() = repository.removeLotList()
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
    suspend fun deleteBagDetails() = repository.deleteBagDetails()
    suspend fun getBagItems(batchNumber: String, material: String) = repository.getBagItems(batchNumber, material)
    suspend fun getAllBagItems() = repository.getAllBagItems()
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getWeighScaleInfo(whId: String, stoNumber: String) = repository.getWeighScaleInfo(whId, stoNumber)
    suspend fun updateAllSyncStatus(model: VegaCocoaMtntWithLots) =
        repository.updateAllSync(model)
    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

     suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>> = repository.getMaterials()




}
