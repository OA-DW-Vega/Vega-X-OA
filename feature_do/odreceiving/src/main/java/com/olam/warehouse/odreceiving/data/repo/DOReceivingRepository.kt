package com.olam.warehouse.odreceiving.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.dorigin.dao.DOReceivingDao
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOReceivingMtnWrapper
import com.olam.warehouse.master.dorigin.model.DOReceivingWarehouseWithMtns
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.data.api.DOReceivingApi
import com.olam.warehouse.odreceiving.data.domain.model.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
interface DOReceivingRepository {
    suspend fun postReceivingDetail(receivingData: DOReceivingPost): LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>>
    suspend fun postReceivingMtnDetail(receivingData: DOReceivingPost): LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>>
    suspend fun getProducts(): LiveData<List<DOMaterial>>
    suspend fun getSuppliers(): LiveData<List<DOVendor>>
    suspend fun getLocations(): LiveData<List<DOStorageLocation>>
    suspend fun getMaterials(): LiveData<List<DOPackageMaterial>>
    suspend fun getSAPMaterials(): LiveData<List<DOMaterial>>
    suspend fun saveReceiving(receivingData: DOReceiving)
    suspend fun getReceiving(): LiveData<List<DOReceiving>>
    suspend fun getWarehouses(): LiveData<List<DOReceivingWarehouse>>
    suspend fun getWarehousesWithMtns(whID: String): LiveData<DOReceivingWarehouseWithMtns>
    suspend fun getDOBags(txnId: String?, allBag: Boolean?= false): LiveData<List<DOBag>>

    suspend fun saveTransactionDetail(transactionDetail: DOTransactionDetail)
    suspend fun saveBagDetail(bag: DOBag)

    //suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<DOReceivingMtnWrapper>>>
    suspend fun saveWarehouseWithMtns(it: DOReceivingMtnWrapper)
    suspend fun getDispatchDetails(key: String, dispatchDetailPost: DODispatchDetailPost): LiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>>

    suspend fun saveReceivingLineItems(lineItems: List<DOReceivingLineItem>)
    suspend fun getReceivingWithLineItem(): LiveData<List<DOReceivingWithLineItems>>
    suspend fun updateDeletedItem(wbid: String, txnId: String?)
    suspend fun deleteReceiving(receivingData: DOReceiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String)

    suspend fun getTransactionDetail(bagQrCode: String, id: String): LiveData<Resource<GenericReqAndResp<DOTxnDetail>>>
    suspend fun getTransactionDetailOffline(id: String): LiveData<DOTransactionDetail>
    fun updateTransactionDetails(lotTransactionId: String)
    suspend fun getTransactions(): LiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>>
    suspend fun getTransactionsOffline(): LiveData<List<DOTransactionDetail>>
    suspend fun getCustomLocations(): LiveData<List<DOCustomStLocation>>
    suspend fun getAllDOBagsInfo(): LiveData<List<DOBag>>
    suspend fun getDispatchDetailsOffline(): LiveData<List<DispatchDetail>>
    suspend fun deleteBag(lotTransactionId: String?, currentQrCode: String)
    suspend fun deleteBag(doBag: DOBag)
    suspend fun getODSapMaterial(): LiveData<Resource<GenericReqAndResp<List<DOSapMaterialList>>>>
    suspend fun getOfflineSapMaterialByProductList():LiveData<List<DOSapMaterialList>>

}

class DOReceivingRepositoryImpl(
    private val api: DOReceivingApi,
    private val dao: DOReceivingDao,
    private val masterDao: MasterDao
) : DOReceivingRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun updateDeletedItem(wbid: String, txnId: String?) {
        dao.deleteItemReceiving(wbid)
        txnId?.let { dao.updateDeletedTransactionDetail(it) }
        masterDao.updateWBDB(wbid)
        masterDao.deleteOfflineParams(wbid)
    }

    override suspend fun getDispatchDetails(key: String, dispatchDetailPost: DODispatchDetailPost): LiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DispatchDetailsResponse>>() {
            override suspend fun createCall() = api.getDispatchDetails(currentKey, dispatchDetailPost)
        }.build().asLiveData()
    }




    override suspend fun getReceivingWithLineItem() = dao.getReceivingWithLineItem(currentKey)
    override suspend fun saveReceivingLineItems(lineItems: List<DOReceivingLineItem>) =
        dao.saveReceivingLineItems(lineItems)


    override suspend fun saveWarehouseWithMtns(it: DOReceivingMtnWrapper) = dao.saveWarehouseAndMtns(it)
    override suspend fun getWarehouses() = dao.getWarehouses()
    override suspend fun getWarehousesWithMtns(whID: String) = dao.getWarehousesWithMtns(whID)
    override suspend fun getDOBags(txnId: String?, allBag: Boolean?): LiveData<List<DOBag>> {
        return if (allBag != null && allBag) {
            dao.getAllDOBags(txnId!!)
        } else {
            dao.getDOBags(txnId!!)
        }
    }


    override suspend fun getReceiving() = dao.getReceiving()
    override suspend fun saveReceiving(receivingData: DOReceiving) = dao.save(receivingData)

    override suspend fun getDispatchDetailsOffline(): LiveData<List<DispatchDetail>> = dao.getDispatchDetails()
    override suspend fun deleteBag(lotTransactionId: String?, currentQrCode: String) = dao.deleteBag(lotTransactionId, currentQrCode)
    override suspend fun deleteBag(doBag: DOBag) = dao.deleteBag(doBag)

    override suspend fun getODSapMaterial(): LiveData<Resource<GenericReqAndResp<List<DOSapMaterialList>>>> {
    return object :NetworkOnlyBoundResource<GenericReqAndResp<List<DOSapMaterialList>>>(){
        override suspend fun createCall()= api.getSapMaterialByProductCode(currentKey)
    }.build().asLiveData()
    }

    override suspend fun saveTransactionDetail(transactionDetail: DOTransactionDetail) = dao.insertTransactionDetail(transactionDetail)
    override suspend fun saveBagDetail(bag: DOBag) = dao.insertBag(bag)

    override suspend fun deleteReceiving(receivingData: DOReceiving) = dao.deleteItemReceiving(receivingData.tmpWbId)
    override suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) {
        dao.updateReceivingFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
        dao.updateReceivingLineItemFailMsg(msg, tmpWbId, Status.SYNC_ERROR)
    }

    override suspend fun getSuppliers() = dao.getSuppliers()
    override suspend fun getLocations() = dao.getLocations()
    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getMaterials() = dao.getMaterials()
    override suspend fun getSAPMaterials() = dao.getSAPMaterials()

    override suspend fun postReceivingDetail(receivingData: DOReceivingPost): LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<DOReceivingResponse> =
                api.postReceivingDetail(receivingData)
        }.build().asLiveData()
    }

    override suspend fun postReceivingMtnDetail(receivingData: DOReceivingPost): LiveData<Resource<GenericReqAndResp<DOReceivingResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOReceivingResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<DOReceivingResponse> =
                api.postReceivingMtnDetail(receivingData)
        }.build().asLiveData()
    }

    /*override suspend fun fetchWarehouseWithMtns(): LiveData<Resource<GenericReqAndResp<DOReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<DOReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns()
        }.build().asLiveData()
    }*/

    override suspend fun getTransactionDetail(bagQrCode: String, id: String): LiveData<Resource<GenericReqAndResp<DOTxnDetail>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<DOTxnDetail>>() {
//            override suspend fun createCall() = api.getTransactionDetail(id, id, currentKey)
            override suspend fun createCall(): GenericReqAndResp<DOTxnDetail> = if (bagQrCode == "0") api.getTransactionDetail(id, currentKey) else api.getTransactionDetailBag(bagQrCode, currentKey)
        }.build().asLiveData()
    }

    override suspend fun getTransactionDetailOffline(id: String) = dao.getTransactionDetailOffline(id)

    override fun updateTransactionDetails(lotTransactionId: String) = dao.updateTransactionDetails(lotTransactionId)

    override suspend fun getTransactions(): LiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<DOTransactionDetail>>>() {
            override suspend fun createCall() = api.fetchTransactionList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getTransactionsOffline() = dao.getTransactionsOffline()
    override suspend fun getCustomLocations() = dao.getCustomLocations()
    override suspend fun getAllDOBagsInfo() = dao.getAllDOBagsInfo()

    override suspend fun getOfflineSapMaterialByProductList()= dao.getSapMaterialByProductList()

}
