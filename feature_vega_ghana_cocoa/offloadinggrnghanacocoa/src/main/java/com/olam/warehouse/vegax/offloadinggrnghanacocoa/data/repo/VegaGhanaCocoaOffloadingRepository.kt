package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorOffloadingDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuaOffloadingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.api.VegaGhanaCocoaOffloadingApi
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaWeighScalePallet
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.prepareData
import java.util.*

interface VegaGhanaCocoaOffloadingRepository {
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getMaterialStorageLocation(): List<VegaStorageLocationDetail>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
    suspend fun getStorageLocation(): LiveData<List<VegaCustomStLocation>>
    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>>
    suspend fun getTransactions(): LiveData<List<VegaCoffeeReceivingMtnrWithLots>>
    suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>>
    suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int, tmpWbId: String)
    suspend fun getSavedBagItems(): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun clearBagDetails()
    suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>>
    suspend fun postGhanaCocoaOffloadingDetail(receivingData: VegaGhanaCocoaOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun saveOffloading(receivingData: VegaReceiving)
    suspend fun updateSyncedMtnrDeletedItem()
    suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>)
    suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun deleteOffloadingItem()
    suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>>
    suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun updateDeletedItem(tmpWbId: String)
    suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String)
    suspend fun deleteStatus(mtnNumber: String)


    suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun saveReceiving(receivingData: VegaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getMTNRs(): LiveData<List<VegaReceivingMtn>>
    suspend fun getOfflineLots(): LiveData<List<VegaReceivingMtnLots>>
    suspend fun getStorageLoc(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun deleteMtnrQuality(wbId: String)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getMtnrWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>>
    suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>>

    suspend fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots)
    suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>>
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaGhanaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>
    suspend fun updateStatus(mtnNumber: String)
    suspend fun updateOBD(mtnNumber: String, flag: Boolean)
}

class VegaGhanaOffloadingRepositoryImpl(
    private val apiCocoa: VegaGhanaCocoaOffloadingApi,
    private val dao: VegaEcuadorOffloadingDao,
    private val daoe: VegaCoffeeOffloadDao,
    private val masterDao: MasterDao
) : VegaGhanaCocoaOffloadingRepository {
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getMaterialStorageLocation() = dao.getMaterialStlocDetails()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getuomDetail() = dao.getuomDetail()

    override suspend fun getStorageLocationDetail(): LiveData<List<VegaStorageLocationDetail>> = dao.getStorageLocationDetail()

    override suspend fun getStorageLocation() = dao.getCustomLocations()

    override suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>> = dao.getStockListOffline()

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                apiCocoa.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getStocks(material: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaRminLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaRminLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaRminLots>> =
                    apiCocoa.getStocks(currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getPOListLocal(): LiveData<List<VegaEcuadorPurchaseOrder>> = dao.getPOListLocal()

    override suspend fun saveBagDetails(material: VegaEcuadorOffloadingBagMaterial) = dao.saveBagDetails(material)
    override suspend fun deleteBagDetails(id: Int, tmpWbId: String) = dao.deleteBagDetails(id, tmpWbId)
    override suspend fun clearBagDetails() = dao.clearBagDetails()
    override suspend fun getSavedBagItems(): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return dao.getSavedBagItems(getPlantDetails().plantId)
    }

    override suspend fun getBagItems(
        materialCode: String?,
        supplierCode: String,
        type: String,
        poId: String,
        tmpWbId: String
    ): LiveData<List<VegaEcuadorOffloadingBagMaterial>> {
        return if (poId.isEmpty()) dao.getBagItems(materialCode, supplierCode, type, tmpWbId) else dao.getBagItems(
            materialCode,
            supplierCode,
            type,
            poId,
            tmpWbId
        )
    }

    override suspend fun postGhanaCocoaOffloadingDetail(receivingData: VegaGhanaCocoaOffloadingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                apiCocoa.postGhanaCocoaOffloadingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun getTransactions() = dao.getTransactions()

    override suspend fun saveOffloading(receivingData: VegaReceiving) = dao.saveOffloading(receivingData)
    override suspend fun saveReceivingLineItems(bagList: ArrayList<VegaEcuadorOffloadingBagMaterial>) =
        dao.saveReceivingLineItems(bagList)

    override suspend fun getOffloadingWithLineItem(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItem()

    override suspend fun deleteOffloadingItem() {
            dao.deleteOffloadingItem()
     }

    override suspend fun getOffloadingWithLineItemCount(): LiveData<List<VegaEcuaOffloadingWithLineItems>> =
        dao.getOffloadingWithLineItemCount()

    override suspend fun updateDeletedItem(tmpWbId: String) {
        dao.deleteOffloadingItem(tmpWbId)
        dao.deleteBagItem(tmpWbId)
        if (tmpWbId.contains("TMP")) {
            dao.deleteGrnData(tmpWbId)
            dao.updateDeletedItem(tmpWbId)
        }
    }

    override suspend fun updateWBToQualityAndGrnTable(tmpWbid: String, wbid: String) {
        dao.updateTempIdToWbid(tmpWbid, wbid)
    }

    override suspend fun getProcessTypeList(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getProcessTypeList(role)



    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun updateDeletedItem(wbid: String, txnId: String?) {
        dao.deleteItemReceiving(wbid)
        masterDao.updateWBDB(wbid)
        masterDao.deleteOfflineParams(wbid)
    }

    override suspend fun getReceivingWithLineItem() = dao.getReceivingWithLineItem()
    override suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) {
        dao.deleteOfflineReceivingLineItem(lineItems[0].tmpWbId)
        dao.saveReceivingLineItems(lineItems)
    }

    override suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper) = dao.saveWarehouseAndMtns(it)
    override suspend fun getWarehouses() = dao.getWarehouses()
    override suspend fun getMTNRs() = dao.getMTNRs()
    override suspend fun getOfflineLots() = dao.getOfflineLots()
    override suspend fun getStorageLoc() = dao.getStorageLoc()
    override suspend fun getWarehousesWithMtns(whID: String) = dao.getWarehousesWithMtns(whID)
    override suspend fun getReceiving() = dao.getReceiving()
    override suspend fun saveReceiving(receivingData: VegaReceiving) = dao.saveOffloading(receivingData)
    override suspend fun deleteReceiving(receivingData: VegaReceiving) =
        dao.deleteItemReceiving(receivingData.tmpWbId)

    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        dao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        dao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }


    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
     override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                apiCocoa.postReceivingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                apiCocoa.postReceivingMtnDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper> =
                apiCocoa.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }


    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                apiCocoa.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getMtnrWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityWBDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityWBDetails>> =
                apiCocoa.fetchMtnrWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    /* override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<List<VegaReceiving>>> {

         return object : NetworkBoundResource<List<VegaReceiving>, GenericReqAndResp<List<VegaReceiving>>>() {

             override fun processResponse(response: GenericReqAndResp<List<VegaReceiving>>): List<VegaReceiving> =
                 response.data

             override suspend fun saveCallResults(items: List<VegaReceiving>) = dao.saveReceiving(items, DIRECTIONOUT)

             override fun shouldFetch(data: List<VegaReceiving>?): Boolean = true

             override suspend fun loadFromDb(): List<VegaReceiving> =
                 dao.getWeighBridgeDetailOnline(Status.SYNC_PENDING, "OUT")

             override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                 api.fetchWeighBridgeDetail(currentKey)

         }.build().asLiveData()
     }*/

    override suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                apiCocoa.fetchTruckInWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                apiCocoa.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>> {
        return if (batchNumber.isNullOrEmpty()) daoe.getBagItems() else daoe.getBagItems(batchNumber, mtnNumber)
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = daoe.saveBagDetails(bagMaterial)
    override suspend fun deleteBagDetails(id: Int) = daoe.deleteBagDetails(id)
    override suspend fun deleteMtnrQuality(wbId: String) = dao.deleteMtnrQuality(wbId)
    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaWeighScalePallet>> =
                apiCocoa.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getOBDDetails(deliveryNumber: String) = dao.getOBDDetails(deliveryNumber)
    override suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) {
        if (vegaCoffeeReceivingData.tempWBId?.startsWith("TMP", false)!!) {
            dao.saveMtnrQualityList(prepareData(vegaCoffeeReceivingData))
        }
        dao.saveMtnrReceiving(vegaCoffeeReceivingData)
        if (lot.batch.isNotEmpty())
            dao.saveMtnrReceivingLot(lot)
    }

    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaGhanaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                apiCocoa.postOffloadingDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun updateStatus(mtnNumber: String) {
        dao.deleteReceivingItem(mtnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
    }

    override suspend fun updateOBD(mtnNumber: String, flag: Boolean) {
        dao.updateOBD(mtnNumber, flag)
    }

    override suspend fun deleteStatus(mtnNumber: String) {
        dao.deleteMtnrReceivingItem(mtnNumber)
        dao.deleteMtnrReceivingLotItem(mtnNumber)
        dao.deleteMtnrReceivingLotItemWithBagItems(mtnNumber)
    }

    override suspend fun updateSyncedMtnrDeletedItem() {
        dao.deleteSyncedMtnrs()

    }
}
