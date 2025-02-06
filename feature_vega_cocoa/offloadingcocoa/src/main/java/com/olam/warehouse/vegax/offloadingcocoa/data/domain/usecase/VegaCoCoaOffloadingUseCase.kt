package com.olam.warehouse.vegax.offloadingcocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCocoOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.repo.VegaCoCoaOffloadingRepository

class VegaCoCoaOffloadingUseCase(private val repository: VegaCoCoaOffloadingRepository) {

    suspend fun getMTNRs() = repository.getMTNRs()
    suspend fun getOfflineLots() = repository.getOfflineLots()
    suspend fun getCoCoaWBDetails(weighBridgeId: String) = repository.getCoCoaWBDetails(weighBridgeId)
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getOfflineStorageLocations() = repository.getOfflineStorageLocations()
    suspend fun getPlants() = repository.getPlants()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost) = repository.postReceivingDetail(receivingData)
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost) =
        repository.postReceivingMtnDetail(receivingData)

    suspend fun saveReceiving(receivingData: VegaCoCoaReceiving) = repository.saveReceiving(receivingData)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>> = repository.getReceiving()
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>> = repository.getWarehouses()
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns> =
        repository.getWarehousesWithMtns(whID)

    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun saveWarehouseWithMtns(it: VegaCoCoaReceivingMtnWrapper) = repository.saveWarehouseWithMtns(it)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) =
        repository.saveReceivingLineItems(lineItems)

    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>> =
        repository.getReceivingWithLineItem()

    suspend fun updateDeletedItem(wbid: String, txnId: String?) = repository.updateDeletedItem(wbid, txnId)
    suspend fun deleteReceiving(receiving: VegaReceiving) = repository.deleteReceiving(receiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) = repository.updateReceivingFailMsg(msg, tmpWbId)
    suspend fun updateWeight(grossWeight: String, truckoutWeight: String, netWeight: String, deliveryNumber: String) =
        repository.updateWeight(grossWeight, truckoutWeight, netWeight, deliveryNumber)

    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getTruckInWeighBridgeDetailOnline() = repository.getTruckInWeighBridgeDetailOnline()
    suspend fun geCocoaWeighBridgeIdDetail(wbid: String) = repository.geCocoaWeighBridgeIdDetail(wbid)
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String, weighBridgeId: String) = repository.getBagItems(batchNumber, mtnNumber, weighBridgeId)
    suspend fun saveBagDetails(bagMaterial: VegaCoCoaOffloadingBagMaterial) = repository.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String) =
        repository.deleteBagDetails(batchNumber, mtnNumber)

    suspend fun deleteBagDetails(bagtype: String) =
        repository.deleteBagDetails(bagtype)

    suspend fun deleteBagDetailsById(id: String) =
        repository.deleteBagDetailsById(id)


    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getOBDDetails(deliveryNumber: String) = repository.getOBDDetails(deliveryNumber)

    suspend fun getTransactions() = repository.getTransactions()
    suspend fun getPendingList() = repository.getPendingList()
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoCoaReceiving, lot: VegaCoCoaReceiveLots) =
        repository.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)

    suspend fun postOffloading(vegaOffloadingPost: VegaCoCoaOffloadingPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun updateStatus(mtnNumber: String) = repository.updateStatus(mtnNumber)
    suspend fun deleteStatus(grnNumber: String, mtnNumber: String) = repository.deleteStatus(grnNumber, mtnNumber)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()
    suspend fun getVendorInfo(vendorId: String) = repository.vendorInfo(vendorId)
    suspend fun postOffloadingSupplierDetail(vegaOffloadingPost: VegaCocoOffloadingSupplierPostRequest) = repository.postOffloadingSupplierDetail(vegaOffloadingPost)

    suspend fun getProductByName(materialName: String) = repository.getProductByName(materialName = materialName)

}

