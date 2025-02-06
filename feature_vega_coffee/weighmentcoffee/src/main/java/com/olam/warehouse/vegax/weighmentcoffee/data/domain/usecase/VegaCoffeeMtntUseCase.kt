package com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaMtntPost
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.vegax.weighmentcoffee.data.repo.VegaCoffeeMtntRepository


class VegaCoffeeMtntUseCase(private val repository: VegaCoffeeMtntRepository) {
    suspend fun getWeighBridgeDetail(isSales: Boolean,isThirdParty: Boolean) = repository.getWeighBridgeDetail(isSales,isThirdParty)
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getProducts() = repository.getProducts()
    suspend fun postMtntData(mtntData: VegaMtntPost) = repository.postMtntData(mtntData)
    suspend fun postThirdPartyData(mtntData: VegaMtntPost) = repository.postThirdPartyData(mtntData)
    suspend fun saveMtnt(mtntData: VegaMtnt) = repository.saveMtnt(mtntData)
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        repository.saveMtntLineItems(lineItems)

    suspend fun getPurchaseOrder() = repository.getPurchaseOrder()
    suspend fun getMaterials() = repository.getMaterials()

    suspend fun getLocations() = repository.getLocations()
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost) = repository.postReceivingDetail(receivingData)
    suspend fun postSalesDetail(receivingData: VegaReceivingPost) = repository.postSaleTruckInData(receivingData)
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost) =
        repository.postReceivingMtnDetail(receivingData)

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
    suspend fun saveTruckInDetails(receiving: VegaReceiving) = repository.saveTruckInData(receiving)
    suspend fun getReceivingByCommonId(commonId: String) = repository.getReceivingByCommonPrimaryId(commonId)
    suspend fun saveReceiveBagInfo(receive: VegaReceiving, list: List<VegaCoffeeOffloadingBagMaterial>) =
        repository.saveCoffeeReceivingAndBags(receive, list)

    suspend fun getTruckOutInfo(weighId: String) = repository.getTruckOutBagList(weighId)
    suspend fun postSalesTruckOutDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest, isThirdParty: Boolean) =
        repository.postSalesTruckOutDetails(vegaDeliveryPost, isThirdParty)

    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repository.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getPOList() = repository.getPOList()
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
            repository.getQualityParams(materialId, valueExist, wbId)
}
