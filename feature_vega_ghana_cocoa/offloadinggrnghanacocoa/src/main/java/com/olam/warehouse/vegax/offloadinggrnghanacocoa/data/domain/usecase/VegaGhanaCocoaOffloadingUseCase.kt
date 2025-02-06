package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaGhanaCocoaOffloadingPost
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWInventoryModelResponse
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWManualModelResponse
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.repo.VegaGhanaCocoaOffloadingRepository


class VegaGhanaCocoaOffloadingUseCase(private val repository: VegaGhanaCocoaOffloadingRepository) {
    suspend fun getProducts() = repository.getProducts()
    suspend fun getMaterialStorageLocation() = repository.getMaterialStorageLocation()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getuomDetail() = repository.getuomDetail()
    suspend fun getStorageLocation() = repository.getStorageLocation()
    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>> =
        repository.getStockListOffline()

    suspend fun getStocks(material: String, isLiveStock: Boolean) =
        repository.getStocks(material, isLiveStock)

    suspend fun getPOList() = repository.getPOList()
    suspend fun getTransactions() = repository.getTransactions()
    suspend fun getPOListLocal() = repository.getPOListLocal()
    suspend fun getSavedBagItems() = repository.getSavedBagItems()
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

    suspend fun postGhanaCocoaOffloadingDetail(receivingData: VegaGhanaCocoaOffloadingPost) =
        repository.postGhanaCocoaOffloadingDetail(receivingData)

    suspend fun updateSyncedMtnrDeletedItem() = repository.updateSyncedMtnrDeletedItem()

    suspend fun saveOffloading(receivingData: VegaReceiving) = repository.saveOffloading(receivingData)
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        repository.saveReceivingLineItems(bagList)

    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItem()

    suspend fun deleteOffloadingItem() =
        repository.deleteOffloadingItem()

    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        repository.getOffloadingWithLineItemCount()

    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        repository.getProcessTypeList(role)

    suspend fun updateDeletedItem(tmpWbId: String) = repository.updateDeletedItem(tmpWbId)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) =
        repository.updateWBToQualityAndGrnTable(tmpWbid, wbid)

    suspend fun deleteStatus(mtnNumber: String) = repository.deleteStatus(mtnNumber)


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
    suspend fun deleteMtnrQuality(wbId: String) = repository.deleteMtnrQuality(wbId)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) = repository.updateReceivingFailMsg(msg, tmpWbId)
    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()
    suspend fun getMtnrWeighBridgeDetailOnline() = repository.getMtnrWeighBridgeDetailOnline()
    suspend fun getTruckInWeighBridgeDetailOnline() = repository.getTruckInWeighBridgeDetailOnline()
    suspend fun getWeighBridgeIdDetail(wbid: String) = repository.getWeighBridgeIdDetail(wbid)
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String) = repository.getBagItems(batchNumber, mtnNumber)
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = repository.saveBagDetails(bagMaterial)
    suspend fun deleteBagDetails(id: Int) = repository.deleteBagDetails(id)
    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getOBDDetails(deliveryNumber: String) = repository.getOBDDetails(deliveryNumber)
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots) =
        repository.saveMtnrReceivingLots(vegaCoffeeReceivingData, lot)

    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaGhanaOffloadingPostRequest) =
        repository.postOffloadingDetail(vegaOffloadingPost)

    suspend fun getStorageLocationDetail() : LiveData<List<VegaStorageLocationDetail>> = repository.getStorageLocationDetail()

    suspend fun updateStatus(mtnNumber: String) = repository.updateStatus(mtnNumber)
    suspend fun updateOBD(mtnNumber: String, flag: Boolean) = repository.updateOBD(mtnNumber, flag)

    suspend fun validateNumbers(plantId:String,wrNumber: String, wbType: String)
    = repository.validateNumbers(plantId, wrNumber, wbType)

    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)

    suspend fun getDwWithLots(wbId: String) = repository.getMtntWithLots(wbId)
    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaDispatchWB,
        dispatchLotsList: MutableList<VegaGhanaCocoaDispatchLots>
    ) = repository.saveDispatchAndLots(dispatchData, dispatchLotsList)

    suspend fun validateLot(batchNumber: String,key: String): LiveData<Resource
    <GenericReqAndResp<VegaGRNDWLotManualModel>>> =
        repository.validateLot(batchNumber,key)

    suspend fun getDWSLotFromInventory(
        vendorsapcode:String,
        key:String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGRNDWLotManualModel>>>> = repository.getDSWLotDetails(vendorsapcode,key)

    suspend fun getQualityParams(charge: String, material: List<String>, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun insertLot(whId: String, lot: VegaGhanaCocoaDispatchLots) = repository.insertLot(whId, lot)

    suspend fun insertLotList(whId: String, lot: List<VegaGhanaCocoaDispatchLots>) = repository.insertLotList(whId, lot)

    suspend fun insertTruckInfo(dispatch: VegaCocoaDispatchWB) =
        repository.insertTruckInfo(dispatch)

    suspend fun updateStartLoad(startTime: String, whId: String) =
        repository.updateStartLoadTime(startTime, whId)
    suspend fun removeLotFromTruck(batchNumber: String) = repository.removeLot(batchNumber)

    suspend fun getFarmerList() = repository.getFarmerList()

    suspend fun getFeatureMaster() = repository.getFeatureMaster()


}
