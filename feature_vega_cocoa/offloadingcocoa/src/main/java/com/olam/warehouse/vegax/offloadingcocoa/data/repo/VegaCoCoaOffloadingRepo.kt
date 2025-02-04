package com.olam.warehouse.vegax.offloadingcocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.dorigin.entity.DOStorageLocation
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.dao.VegaCoCoaOffloadDao
import com.olam.warehouse.master.vegacocoa.entity.*
import com.olam.warehouse.master.vegacocoa.model.VegaCoCoaReceivingMtnrWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingcocoa.data.api.VegaCoCoaOffloadingApi
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaWeighScalePallet

interface VegaCoCoaOffloadingRepository {
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getPlants(): LiveData<List<VegaCustomStLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun saveReceiving(receivingData: VegaCoCoaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaCoCoaReceivingMtnWrapper)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun updateWeight(grossWeight: String, truckoutWeight: String, netWeight: String, deliveryNumber: String)
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun geCocoaWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>>
    suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoCoaOffloadingBagMaterial>>
    suspend fun saveBagDetails(bagMaterial: VegaCoCoaOffloadingBagMaterial)
    suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String)
    suspend fun deleteBagDetails(bagtype: String)
    suspend fun deleteBagDetailsById(id: String)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>>

    suspend fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoCoaReceivingMtnrWithLots>
    suspend fun getTransactions(): LiveData<List<VegaCoCoaReceivingMtnrWithLots>>
    suspend fun getPendingList(): LiveData<List<VegaCoCoaReceivingMtnrWithLots>>
    suspend fun saveMtnrReceivingLots(vegaCoCoaReceivingData: VegaCoCoaReceiving, lot: VegaCoCoaReceiveLots)
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoCoaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>>
    suspend fun updateStatus(mtnNumber: String)
    suspend fun deleteStatus(grnNumber: String, mtnNumber: String)
    suspend fun getThirdPartyMaterials(): LiveData<List<VegaMaterial>>
    suspend fun vendorInfo(batchNumber: String): LiveData<VegaVendor>
    suspend fun getMTNRs(): LiveData<List<VegaReceivingMtn>>
    suspend fun getOfflineLots(): LiveData<List<VegaReceivingMtnLots>>
    suspend fun getCoCoaWBDetails(weighBridgeId: String): LiveData<VegaCoCoaQualityWBDetail>
    suspend fun getOfflineStorageLocations(): LiveData<List<VegaCoCoaStorageLocation>>
}

class VegaCoCoaOffloadingRepositoryImpl(
    private val api: VegaCoCoaOffloadingApi,
    private val dao: VegaCoCoaOffloadDao,
    private val masterDao: MasterDao
) : VegaCoCoaOffloadingRepository {
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

    override suspend fun saveWarehouseWithMtns(it: VegaCoCoaReceivingMtnWrapper) = dao.saveWarehouseAndMtns(it)
    override suspend fun getWarehouses() = dao.getWarehouses()
    override suspend fun getWarehousesWithMtns(whID: String) = dao.getWarehousesWithMtns(whID)
    override suspend fun getReceiving() = dao.getReceiving()
    override suspend fun saveReceiving(receivingData: VegaCoCoaReceiving) = dao.save(receivingData)
    override suspend fun deleteReceiving(receivingData: VegaReceiving) =
        dao.deleteItemReceiving(receivingData.tmpWbId)

    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        dao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        dao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }

    override suspend fun updateWeight(
        grossWeight: String,
        truckoutWeight: String,
        netWeight: String,
        deliveryNumber: String
    ) = dao.updateWeight(grossWeight, truckoutWeight, netWeight, deliveryNumber)

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getPlants() = dao.getPlants()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingMtnDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoCoaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }


    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.fetchWeighBridgeDetail(currentKey)
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
                api.fetchTruckInWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun geCocoaWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoCoaQualityWBDetail>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoCoaQualityWBDetail> =
                api.geCocoaWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(
        batchNumber: String?,
        mtnNumber: String
    ): LiveData<List<VegaCoCoaOffloadingBagMaterial>> {
        return if (batchNumber.isNullOrEmpty()) dao.getBagItems() else dao.getBagItems(batchNumber, mtnNumber)
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCoCoaOffloadingBagMaterial) = dao.saveBagDetails(bagMaterial)
    override suspend fun deleteBagDetails(batchNumber: String, mtnNumber: String) =
        dao.deleteBagDetails(batchNumber, mtnNumber)

    override suspend fun deleteBagDetails(bagtype: String) =
        dao.deleteBagDetails(bagtype)

    override suspend fun deleteBagDetailsById(id: String) =
        dao.deleteBagDetailsById(id)

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoCoaWeighScalePallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getOBDDetails(deliveryNumber: String) = dao.getOBDDetails(deliveryNumber)
    override suspend fun getTransactions() = dao.getTransactions()
    override suspend fun getPendingList() = dao.getPendingList()

    override suspend fun saveMtnrReceivingLots(
        vegaCoCoaReceivingData: VegaCoCoaReceiving,
        lot: VegaCoCoaReceiveLots
    ) {
        dao.saveMtnrReceiving(vegaCoCoaReceivingData)
        if (lot.batch.isNotEmpty())
            dao.saveMtnrReceivingLot(lot)
    }

    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoCoaOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoCoaOffloadingPostRequest> =
                api.postOffloadingDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun updateStatus(mtnNumber: String) {
        dao.deleteReceivingItem(mtnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
        dao.deleteReceivingLotItemWithBagItems(mtnNumber)
    }

    override suspend fun deleteStatus(grnNumber: String, mtnNumber: String) {
        dao.deleteCoCoaReceivingItem(grnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
        dao.deleteReceivingLotItemWithBagItems(mtnNumber)
    }

    override suspend fun getThirdPartyMaterials() = dao.getThirdPartyMaterials()
    override suspend fun vendorInfo(suppliercode: String) = dao.getVendorInfo(suppliercode)
    override suspend fun getMTNRs() = dao.getMTNRs()
    override suspend fun getOfflineLots() = dao.getOfflineLots()
    override suspend fun getCoCoaWBDetails(weighBridgeId: String) = dao.getCoCoaWBDetails(weighBridgeId)
    override suspend fun getOfflineStorageLocations() = dao.getOfflineStorageLocations()
}
