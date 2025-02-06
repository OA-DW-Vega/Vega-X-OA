package com.olam.warehouse.vegax.weighmentcoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaMtntDao
import com.olam.warehouse.master.vega.dao.VegaReceivingDao
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.*
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.weighmentcoffee.data.api.VegaCoffeeMtntApi
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeSalesPostRequest
import com.olam.warehouse.vegax.weighmentcoffee.utils.prepareData

/**
 * Created by Baskaran Kannan on 9/2/2020.
 */
interface VegaCoffeeMtntRepository {

    suspend fun getWeighBridgeDetail(isSales: Boolean,isThirdParty:Boolean): LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun postMtntData(
        mtntData: VegaMtntPost
    ): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>>

    suspend fun postThirdPartyData(
        mtntData: VegaMtntPost
    ): LiveData<Resource<GenericReqAndResp<Data>>>

    suspend fun saveMtnt(mtntData: VegaMtnt)
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>>
    suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>)
    suspend fun getMaterials(): LiveData<List<VegaPackageMaterial>>
    suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>
    suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>>


    suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun saveReceiving(receivingData: VegaReceiving)
    suspend fun getReceiving(): LiveData<List<VegaReceiving>>
    suspend fun getWarehouses(): LiveData<List<VegaReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<VegaReceivingWarehouseWithMtns>
    suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: VegaReceivingMtnWrapper)
    suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>)
    suspend fun saveTruckInData(receivingData: VegaReceiving)
    suspend fun getReceivingWithLineItem(): LiveData<List<VegaReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: VegaReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)
    suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>>
    suspend fun getLocations(): LiveData<List<VegaSupplyStorageLocation>>
    suspend fun getReceivingByCommonPrimaryId(commonId: String): LiveData<VegaReceiving>
    suspend fun saveCoffeeReceivingAndBags(receive: VegaReceiving, list: List<VegaCoffeeOffloadingBagMaterial>)
    suspend fun getTruckOutBagList(weighBridgeId: String): LiveData<VegaCoffeeTruckOutReceivingWithBags>
    suspend fun postSaleTruckInData(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>>
    suspend fun postSalesTruckOutDetails(
        vegaDeliveryPost: VegaCoffeeSalesPostRequest,
        isThirdParty: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>>

    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>

    suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>>
    suspend fun getQualityParams(
            materialId: String,
            valueExist: Boolean?,
            wbId: String?
    ): LiveData<List<VegaQualityParamsWithQualitative>>
}

class VegaCoffeeMtntRepositoryImpl(
    private val api: VegaCoffeeMtntApi,
    private val dao: VegaMtntDao,
    private val receiveDao: VegaReceivingDao,
    private val masterDao: MasterDao
) :
    VegaCoffeeMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTruckInWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.fetchTruckInWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeDetail(isSales: Boolean,isThirdParty: Boolean): LiveData<Resource<GenericReqAndResp<List<VegaMtnt>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaMtnt>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaMtnt>> =
                api.getWeighBridgeDetail(currentKey, isSales,isThirdParty)
        }.build().asLiveData()
    }


    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getProducts() = dao.getProducts()

    override suspend fun postMtntData(
        mtntData: VegaMtntPost
    ): LiveData<Resource<GenericReqAndResp<VegaMtntResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaMtntResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaMtntResponse> =
                /*if (isThirdParty) api.postThirdPartyTruckOut(mtntData) else*/ api.postMtntData(mtntData)
        }.build().asLiveData()
    }

    override suspend fun postThirdPartyData(mtntData: VegaMtntPost): LiveData<Resource<GenericReqAndResp<Data>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<Data>>() {
            override suspend fun createCall(): GenericReqAndResp<Data> = api.postThirdPartyTruckOut(mtntData)
        }.build().asLiveData()
    }

    override suspend fun saveMtnt(mtntData: VegaMtnt) = dao.insertMtnt(mtntData)

    override suspend fun saveMtntLineItems(lineItems: List<VegaMtntLineItem>) =
        dao.saveMtntLineItems(lineItems)

    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaPurchaseOrder>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getMaterials() = dao.getMaterials()

    override suspend fun getWeighBridgeDetailOnline(): LiveData<Resource<GenericReqAndResp<List<VegaReceiving>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaReceiving>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaReceiving>> =
                api.fetchWeighBridgeDetail(currentKey)
        }.build().asLiveData()
    }

    override suspend fun updateDeletedItem(wbid: String, txnId: String?) {
        receiveDao.deleteItemReceiving(wbid)
        masterDao.updateWBDB(wbid)
        masterDao.deleteOfflineParams(wbid)
    }

    override suspend fun getReceivingWithLineItem() = receiveDao.getReceivingWithLineItem()
    override suspend fun saveReceivingLineItems(lineItems: List<VegaReceivingLineItem>) {
        receiveDao.deleteOfflineReceivingLineItem(lineItems[0].tmpWbId)
        receiveDao.saveReceivingLineItems(lineItems)
    }

    override suspend fun saveWarehouseWithMtns(it: VegaReceivingMtnWrapper) = receiveDao.saveWarehouseAndMtns(it)
    override suspend fun getWarehouses() = receiveDao.getWarehouses()
    override suspend fun getWarehousesWithMtns(whID: String) = receiveDao.getWarehousesWithMtns(whID)
    override suspend fun getReceiving() = receiveDao.getReceiving()
    override suspend fun saveReceiving(receivingData: VegaReceiving) = receiveDao.save(receivingData)
    override suspend fun saveTruckInData(receivingData: VegaReceiving) = receiveDao.saveTruckInData(receivingData)
    override suspend fun deleteReceiving(receivingData: VegaReceiving) =
        receiveDao.deleteItemReceiving(receivingData.tmpWbId)

    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        receiveDao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        receiveDao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }


    override suspend fun getLocations() = receiveDao.getLocations()
    override suspend fun getReceivingByCommonPrimaryId(commonId: String): LiveData<VegaReceiving> =
        receiveDao.getReceivingData(commonId)

    override suspend fun postReceivingDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun postSaleTruckInData(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postSalesData(receivingData)
        }.build().asLiveData()
    }


    override suspend fun postReceivingMtnDetail(receivingData: VegaReceivingPost): LiveData<Resource<GenericReqAndResp<VegaReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingResponse> =
                api.postReceivingMtnDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getWeighBridgeIdDetail(wbid: String): LiveData<Resource<GenericReqAndResp<VegaReceiving>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceiving>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceiving> =
                api.getWeighBridgeIdDetail(currentKey, wbid)
        }.build().asLiveData()
    }

    override suspend fun saveCoffeeReceivingAndBags(
        receive: VegaReceiving,
        list: List<VegaCoffeeOffloadingBagMaterial>
    ) {
        receiveDao.saveReceivingItem(receive)
        receiveDao.saveCoffeeTruckOutBagList(list)
    }

    override suspend fun getTruckOutBagList(weighBridgeId: String): LiveData<VegaCoffeeTruckOutReceivingWithBags> =
        receiveDao.getTruckOutInfo(weighBridgeId)

    override suspend fun postSalesTruckOutDetails(
        vegaDeliveryPost: VegaCoffeeSalesPostRequest,
        isThirdParty: Boolean
    ): LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeSalesPostRequest> =
                api.postSalesTruckOut(
                    vegaDeliveryPost
                )
        }.build().asLiveData()
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

    override suspend fun getPOList(): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>> =
                api.getPurchaseOrders(getCurrentKey())
        }.build().asLiveData()
    }
    override suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?)
            : LiveData<List<VegaQualityParamsWithQualitative>> {
        return if (valueExist!!) prepareData(receiveDao.getQualityParameterWithData(materialId, wbId)) else
            receiveDao.getQualityParameter(materialId)
    }
}
