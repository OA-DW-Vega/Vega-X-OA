package com.olam.warehouse.vegax.weighment.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vega.model.VegaReceivingWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.weighment.data.api.VegaIndiaCoffeeReceivingApi
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingResponse


interface VegaReceivingRepository {
    suspend fun postReceivingDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>>
    suspend fun postReceivingMtnDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun saveReceiving(receivingData: VegaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaReceivingMtnWrapper)

    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getQualityParams(
            materialId: String,
            valueExist: Boolean?,
            wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
    suspend fun getPreSamplingQualityList(
            batchNo: String,
            materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>
}

class VegaReceivingRepositoryImpl(
    private val api: VegaIndiaCoffeeReceivingApi,
    private val dao: VegaReceivingDao,
    private val masterDao: MasterDao
) : VegaReceivingRepository {
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

    override suspend fun saveWarehouseWithMtns(it: VegaReceivingMtnWrapper) = dao.saveWarehouseAndMtns(it)
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
    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun postReceivingDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeReceivingResponse> =
                api.postReceivingDetail(indiaCoffeeReceivingData)
        }.build().asLiveData()
    }

    override suspend fun postReceivingMtnDetail(indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): LiveData<Resource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndiaCoffeeReceivingResponse> =
                api.postReceivingMtnDetail(indiaCoffeeReceivingData)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return dao.getQualityParameter(materialId)
    }

    override suspend fun getPreSamplingQualityList(
            batchNo: String,
            materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>> {
        val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaQualityPreParameter>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaQualityPreParameter>> =
                    api.fetchPreQualityDetails(currentKey, batchNo, materialId)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
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

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }


}
