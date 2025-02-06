package com.olam.warehouse.vegax.nigeriaweighment.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.nigeriaweighment.data.domain.model.VegaNigeriaReceivingPost
import com.olam.warehouse.vegax.nigeriaweighment.data.repo.VegaReceivingRepository

class VegaNigeriaReceivingUseCase(private val repository: VegaReceivingRepository) {

    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postReceivingDetail(nigeriaReceivingData: VegaNigeriaReceivingPost) =
        repository.postReceivingDetail(nigeriaReceivingData)

    suspend fun postReceivingMtnDetail(nigeriaReceivingData: VegaNigeriaReceivingPost) =
        repository.postReceivingMtnDetail(nigeriaReceivingData)

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

    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repository.getQualityParams(materialId, valueExist, wbId)
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)
    suspend fun fetchTruckInWeighBridgeDetail(selectedPlantId: String) = repository.getTruckInWeighBridgeDetailOnline(selectedPlantId)

}
