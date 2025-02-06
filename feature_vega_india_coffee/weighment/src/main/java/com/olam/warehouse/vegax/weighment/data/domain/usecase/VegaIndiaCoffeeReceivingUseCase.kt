package com.olam.warehouse.vegax.weighment.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingPost
import com.olam.warehouse.vegax.weighment.data.repo.VegaReceivingRepository

class VegaIndiaCoffeeReceivingUseCase(private val repository: VegaReceivingRepository) {

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postReceivingDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost) =
        repository.postReceivingDetail(indiaCoffeeReceivingData)

    suspend fun postReceivingMtnDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost) =
        repository.postReceivingMtnDetail(indiaCoffeeReceivingData)

    suspend fun saveReceiving(receivingData: VegaReceiving) = repository.saveReceiving(receivingData)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>> = repository.getReceiving()
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>> = repository.getWarehouses()
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns> =
        repository.getWarehousesWithMtns(whID)

    suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun saveWarehouseWithMtns(it: VegaReceivingMtnWrapper) = repository.saveWarehouseWithMtns(it)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) =
        repository.saveReceivingLineItems(lineItems)

    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>> =
        repository.getReceivingWithLineItem()

    suspend fun updateDeletedItem(wbid: String, txnId: String?) = repository.updateDeletedItem(wbid, txnId)
    suspend fun deleteReceiving(receiving: VegaReceiving) = repository.deleteReceiving(receiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) = repository.updateReceivingFailMsg(msg, tmpWbId)
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getTruckInWeighBridgeDetailOnline() = repository.getTruckInWeighBridgeDetailOnline()
    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
            repository.getQualityParams(materialId, valueExist, wbId)
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
            repository.getPreSamplingQualityList(batchNo, materialId)


}
