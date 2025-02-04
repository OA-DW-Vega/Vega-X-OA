package com.olam.warehouse.vegax.offloadingcoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeOffloadDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.offloadingcoffee.data.api.VegaCoffeeOffloadingApi
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeOffloadingSupplierPostRequest
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.offloadingcoffee.utils.prepareData

interface VegaCoffeeOffloadingRepository {
    suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getStoragetLocations(): LiveData<List<VegaStorageLocation>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun saveReceiving(receivingData: VegaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaCoffeeReceivingMtnWrapper)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>>
    suspend fun getTruckInWeighBridgeDetailOnline(isWeighscale: Boolean): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>>
    suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>

    suspend fun getBagItems(batchNumber: String?, mtnNumber: String): LiveData<List<VegaCoffeeOffloadingBagMaterial>>
    suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial)
    suspend fun deleteBagDetails(id: Int)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>>

    suspend fun getOBDDetails(deliveryNumber: String): LiveData<VegaCoffeeReceivingMtnrWithLots>
    suspend fun saveMtnrReceivingLots(vegaCoffeeReceivingData: VegaCoffeeReceiving, lot: VegaCoffeeReceiveLots)
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoffeeOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>
    suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoffeeOffloadingSupplierPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>
    suspend fun updateStatus(mtnNumber: String)
    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getQualityParams(
        materialId: String,
        valueExist: Boolean?,
        wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
}

class VegaCoffeeOffloadingRepositoryImpl(
    private val api: VegaCoffeeOffloadingApi,
    private val dao: VegaCoffeeOffloadDao,
    private val repo: VegaNicaraguaGrnDao,
    private val masterDao: MasterDao
) : VegaCoffeeOffloadingRepository {
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
    override suspend fun getWarehousesWithMtns(whID: String) = dao.getWarehousesWithMtns(whID)
    override suspend fun getReceiving() = dao.getReceiving()
    override suspend fun saveReceiving(receivingData: VegaReceiving) = dao.save(receivingData)
    override suspend fun deleteReceiving(receivingData: VegaReceiving) =
        dao.deleteItemReceiving(receivingData.tmpWbId)

    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        dao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        dao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getStoragetLocations() =
        repo.getStorageLocations(getPlantDetails().plantId)

    override suspend fun getLocations() = dao.getLocations()
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

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }


    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeReceiving>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }
    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(dao.getQualityParameterWithData(materialId, wbId)) else
            dao.getQualityParameter(materialId)
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

    override suspend fun getTruckInWeighBridgeDetailOnline(isWeighscale: Boolean): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeReceiving>> =
                if (isWeighscale) api.fetchTruckInWeighScaleDetail(currentKey) else api.fetchTruckInWeighBridgeDetail(
                    currentKey
                )
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(
        wbid: String,
        isWeighscale: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                if (isWeighscale) api.getWeighScaleIdDetail(
                    currentKey,
                    wbid
                ) else api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun getBagItems(
        batchNumber: String?,
        mtnNumber: String
    ): LiveData<List<VegaCoffeeOffloadingBagMaterial>> {
        return if (batchNumber.isNullOrEmpty()) dao.getBagItems() else dao.getBagItems(batchNumber, mtnNumber)
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCoffeeOffloadingBagMaterial) = dao.saveBagDetails(bagMaterial)
    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)
    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeWeighScalePallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getOBDDetails(deliveryNumber: String) = dao.getOBDDetails(deliveryNumber)
    override suspend fun saveMtnrReceivingLots(
        vegaCoffeeReceivingData: VegaCoffeeReceiving,
        lot: VegaCoffeeReceiveLots
    ) {
        dao.saveMtnrReceiving(vegaCoffeeReceivingData)
        if (lot.batch.isNotEmpty())
            dao.saveMtnrReceivingLot(lot)
    }

    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoffeeOffloadingPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                api.postOffloadingDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun postOffloadingDetail(vegaOffloadingPost: VegaCoffeeOffloadingSupplierPostRequest): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                api.postOffloadingSupplierDetail(vegaOffloadingPost)
        }.build().asLiveData()
    }

    override suspend fun updateStatus(mtnNumber: String) {
        dao.deleteReceivingItem(mtnNumber)
        dao.deleteReceivingLotItem(mtnNumber)
        dao.deleteReceivingLotItemWithBagItems(mtnNumber)
    }

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }
}
