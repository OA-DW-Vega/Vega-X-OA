package com.olam.warehouse.odreceiving.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.master.dorigin.model.DOReceivingMtnWrapper
import com.olam.warehouse.master.dorigin.model.DOReceivingWarehouseWithMtns
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.data.domain.model.DODispatchDetailPost
import com.olam.warehouse.odreceiving.data.domain.model.DOReceivingPost
import com.olam.warehouse.odreceiving.data.domain.model.DispatchDetailsResponse
import com.olam.warehouse.odreceiving.data.repo.DOReceivingRepository
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOReceivingUseCase(private val repository: DOReceivingRepository) {
    suspend fun getProducts() = repository.getProducts()
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getLocations() = repository.getLocations()
    suspend fun getMaterials() = repository.getMaterials()
    suspend fun getSAPMaterials() = repository.getSAPMaterials()
    suspend fun postReceivingDetail(receivingData: DOReceivingPost) = repository.postReceivingDetail(receivingData)
    suspend fun postReceivingMtnDetail(receivingData: DOReceivingPost) =
        repository.postReceivingMtnDetail(receivingData)

    suspend fun saveReceiving(receivingData: DOReceiving) = repository.saveReceiving(receivingData)
    suspend fun saveTransactionDetail(transactionDetail: DOTransactionDetail) = repository.saveTransactionDetail(transactionDetail)
    suspend fun saveBagDetail(bag: DOBag) = repository.saveBagDetail(bag)
    suspend fun getDOBags(txnId: String?, isAllBag : Boolean? = false) = repository.getDOBags(txnId, isAllBag)
    suspend fun getReceiving(): LiveData<List<DOReceiving>> = repository.getReceiving()
    suspend fun getWarehouses(): LiveData<List<DOReceivingWarehouse>> = repository.getWarehouses()
    suspend fun getWarehousesWithMtns(whID: String): LiveData<DOReceivingWarehouseWithMtns> =
        repository.getWarehousesWithMtns(whID)

    //suspend fun fetchWarehouseWithMtns() = repository.fetchWarehouseWithMtns()
    suspend fun saveWarehouseWithMtns(it: DOReceivingMtnWrapper) = repository.saveWarehouseWithMtns(it)

    suspend fun saveReceivingLineItems(lineItems: List<DOReceivingLineItem>) =
        repository.saveReceivingLineItems(lineItems)

    suspend fun getReceivingWithLineItem(): LiveData<List<DOReceivingWithLineItems>> =
        repository.getReceivingWithLineItem()

    suspend fun updateDeletedItem(wbid: String, txnId: String?) = repository.updateDeletedItem(wbid, txnId)
    suspend fun deleteReceiving(receiving: DOReceiving) = repository.deleteReceiving(receiving)
    suspend fun updateReceivingFailMsg(msg: String, tmpWbId: String) = repository.updateReceivingFailMsg(msg, tmpWbId)

    suspend fun getTransactionDetail(bagQrCode: String, id: String): LiveData<Resource<GenericReqAndResp<DOTxnDetail>>> =
        repository.getTransactionDetail(bagQrCode, id)

    suspend fun getTransactionDetailOffline(id: String): LiveData<DOTransactionDetail> =
        repository.getTransactionDetailOffline(id)

    fun updateTransactionDetails(lotTransactionId: String) = repository.updateTransactionDetails(lotTransactionId)

    suspend fun getTransactions(): LiveData<Resource<GenericReqAndResp<List<DOTransactionDetail>>>> =
        repository.getTransactions()
    suspend fun getSapMaterialByProduct():LiveData<Resource<GenericReqAndResp<List<DOSapMaterialList>>>> =
        repository.getODSapMaterial()


    suspend fun getTransactionsOffline(): LiveData<List<DOTransactionDetail>> =
        repository.getTransactionsOffline()

    suspend fun getCustomLocations() = repository.getCustomLocations()

    suspend fun getAllDOBagsInfo() = repository.getAllDOBagsInfo()

    suspend fun getDispatchDetailsOffline(): LiveData<List<DispatchDetail>> = repository.getDispatchDetailsOffline()

    suspend fun getDispatchDetails(
        key: String,
        dispatchDetailPost: DODispatchDetailPost
    ): LiveData<Resource<GenericReqAndResp<DispatchDetailsResponse>>> =
        repository.getDispatchDetails(key, dispatchDetailPost)

    suspend fun deleteBag(lotTransactionId: String?, currentQrCode: String) =
        repository.deleteBag(lotTransactionId, currentQrCode)

    suspend fun deleteBag(doBag: DOBag) = repository.deleteBag(doBag)

    suspend fun getOfflineODSapMaterialByProduct():LiveData<List<DOSapMaterialList>> = repository.getOfflineSapMaterialByProductList()

}
