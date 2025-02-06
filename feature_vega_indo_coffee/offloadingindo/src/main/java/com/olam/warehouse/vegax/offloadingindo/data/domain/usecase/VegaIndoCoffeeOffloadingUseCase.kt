package com.olam.warehouse.vegax.offloadingindo.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.vegax.offloadingindo.data.domain.model.VegaIndoCoffeeOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingindo.data.domain.model.VegaIndoCoffeeOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingindo.data.repo.VegaIndoCoffeeOffloadingRepository

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */

class VegaIndoCoffeeOffloadingUseCase(private val repository: VegaIndoCoffeeOffloadingRepository) {

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost) = repository.postReceivingDetail(receivingData)
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost) =
        repository.postReceivingMtnDetail(receivingData)

    suspend fun saveReceiving(receivingData: VegaReceiving) = repository.saveReceiving(receivingData)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>> = repository.getReceiving()
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>> = repository.getWarehouses()
    suspend fun getMTNRs() = repository.getMTNRs()
    suspend fun getOfflineLots() = repository.getOfflineLots()
    suspend fun getStorageLoc() = repository.getStorageLoc()
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns> =
        repository.getWarehousesWithMtns(whID)

    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper) = repository.saveWarehouseWithMtns(it)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) =
        repository.saveReceivingLineItems(lineItems)

    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>> =
        repository.getReceivingWithLineItem()

    suspend fun updateDeletedItem(wbid: String, txnId: String?) = repository.updateDeletedItem(wbid, txnId)
    suspend fun deleteReceiving(receiving: VegaReceiving) = repository.deleteReceiving(receiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) = repository.updateReceivingFailMsg(msg, tmpWbId)
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getTruckInWeighBridgeDetailOnline(isWeighscale: Boolean) =
        repository.getTruckInWeighBridgeDetailOnline(isWeighscale)

    suspend fun getWeighBridgeIdDetail(wbid: String, isWeighscale: Boolean) =
        repository.getWeighBridgeIdDetail(wbid, isWeighscale)

    suspend fun getBagItems(tmpWbId: String, batchNumber: String) = repository.getBagItems(tmpWbId, batchNumber)
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = repository.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getOBDDetails(deliveryNumber: String) = repository.getOBDDetails(deliveryNumber)
    suspend fun getOBDDetailsIndo(tmpWbId: String) = repository.getOBDDetailsIndo(tmpWbId)
    suspend fun getOBDDetailsSupIndo(tmpWbId: String) = repository.getOBDDetailsSupIndo(tmpWbId)
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots) =
        repository.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)

    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaIndoCoffeeOffloadingPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun postOffloadingSupplierDetail(vegaOffloadingPost: VegaIndoCoffeeOffloadingSupplierPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun updateStatus(tmpWbId: String, msg: String, vegaMtntResponse: VegaMtntResponse) =
        repository.updateStatus(tmpWbId, msg, vegaMtntResponse)

    suspend fun getPOList() = repository.getPOList()

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)

    suspend fun getOffloadingItem() = repository.getOffloadingItem()
    suspend fun deleteAllItem(tmpWbId: String) = repository.deleteAllItem(tmpWbId)
}
