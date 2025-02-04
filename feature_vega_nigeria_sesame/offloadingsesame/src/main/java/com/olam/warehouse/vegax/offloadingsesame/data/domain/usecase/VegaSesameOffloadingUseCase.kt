package com.olam.warehouse.vegax.offloadingsesame.data.domain.usecase

import androidx.lifecycle.LiveData
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
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.vegax.offloadingsesame.data.domain.usecase.model.VegaSesameOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingsesame.data.repo.VegaSesameOffloadingRepository
import java.util.*

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
class VegaSesameOffloadingUseCase(private val repository: VegaSesameOffloadingRepository) {
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getStorageLocation() = repository.getStorageLocation()
    suspend fun getPOList() = repository.getPOList()
    suspend fun getPOListLocal() = repository.getPOListLocal()

    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = repository.saveBagDetails(material)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String) = repository.deleteBagDetails(id, tmpWbId)
    suspend fun clearBagDetails() = repository.clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ) = repository.getBagItems(materialCode, supplierCode, type, poId, tmpWbId)
    suspend fun postEcuadorOffloadingDetail(receivingData: VegaReceivingPost) =
        repository.postEcuadorOffloadingDetail(receivingData)

    suspend fun saveOffloading(receivingData: VegaReceiving) = repository.saveOffloading(receivingData)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        repository.saveReceivingLineItems(bagList)

    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItem()

    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItemCount()

    suspend fun updateDeletedItem(tmpWbId: String) = repository.updateDeletedItem(tmpWbId)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) =
        repository.updateWBToQualityAndGrnTable(tmpWbid, wbid)



    suspend fun getLocations() = repository.getLocations()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost) = repository.postReceivingDetail(receivingData)
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost) =
        repository.postReceivingMtnDetail(receivingData)

    suspend fun saveReceiving(receivingData: VegaReceiving) = repository.saveReceiving(receivingData)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>> = repository.getReceiving()
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>> = repository.getWarehouses()
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
    suspend fun getTruckInWeighBridgeDetailOnline() = repository.getTruckInWeighBridgeDetailOnline()
    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String) = repository.getBagItems(batchNumber, mtnNumber)
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = repository.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getOBDDetails(deliveryNumber: String) = repository.getOBDDetails(deliveryNumber)
    suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) =
        repository.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)

    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaSesameOffloadingPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun updateStatus(mtnNumber: String) = repository.updateStatus(mtnNumber)

    suspend fun getMTNRs() = repository.getMTNRs()

    suspend fun getOfflineLots() = repository.getOfflineLots()
    suspend fun getStorageLoc() = repository.getStorageLoc()
    suspend fun getTransactions() = repository.getTransactions()


}
