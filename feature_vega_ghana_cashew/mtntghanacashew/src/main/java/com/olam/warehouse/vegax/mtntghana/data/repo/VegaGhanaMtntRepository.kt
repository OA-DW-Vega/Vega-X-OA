package com.olam.warehouse.vegax.mtntghana.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.master.vegaecuador.dao.VegaEcuadorDispatchDao
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchPurchaseOrders
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.mtntghana.data.api.VegaGhanaMtntApi

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */
interface VegaGhanaMtntRepository {
    suspend fun getPurchaseOrder(plantId: String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>>
    suspend fun getPurchaseOrderLocal(): LiveData<List<VegaCocoaPurchaseOrders>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaEcuadorDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>>
    suspend fun validateLot(batchNumber: String, tnpId: String): VegaEcuadorDispatchLots
    suspend fun updateRemarks(remark: String, batchNumber: String)
    suspend fun getLotDetails(
        charge: String,
        materialList: ArrayList<String>,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>

    suspend fun getLotDetailsLocal(charge: String): LiveData<List<VegaEcuadorDispatchStocks>>
    suspend fun getStockList(materialList: java.util.ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>>
    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>

    suspend fun saveLots(
        dispatchLotsList: MutableList<VegaEcuadorDispatchLots>,
        model: VegaEcuadorDispatch
    )
    suspend fun deleteLot(batchNumber: String, wbTempId: String)
    suspend fun insertLot(lot: VegaEcuadorDispatchLots)
     fun getDispatchLots(tmpId: String): LiveData<List<VegaEcuadorDispatchLots>>
    suspend fun saveDispatch(dispatchData: VegaEcuadorDispatch)
    suspend fun saveDispatchLotLineItems(lotList: ArrayList<VegaEcuadorDispatchLots>)
    suspend fun getDispatchWithLineItem(): LiveData<List<VegaEcuadorDispatchWithLineItems>>
    suspend fun getDispatchWithLineItemCount(): LiveData<List<VegaEcuadorDispatchWithLineItems>>
    suspend fun updateDeletedItem(tmpWbId: String)
}

class VegaEcuadorDispatchRepositoryImpl(
    private val api: VegaGhanaMtntApi,
    private val dao: VegaEcuadorDispatchDao,
    private val masterDao: MasterDao
) : VegaGhanaMtntRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getPurchaseOrder(plantId: String): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>> =
                api.getPurchaseOrder(currentKey, plantId)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrderLocal() = dao.getPurchaseOrderOffline()

    override suspend fun getProducts() = dao.getProducts()

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaEcuadorDeliveryPost): LiveData<Resource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaEcuadorDeliveryPostResponse> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun validateLot(whId: String, tmpId: String) = dao.validateLotAlreadyAdded(whId, tmpId)
    override suspend fun updateRemarks(remark: String, batchNumber: String) = dao.updateRemark(remark, true, batchNumber)

    override suspend fun getLotDetails(
        charge: String,
        materialList: ArrayList<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorDispatchStocks>> =
                api.getLot(currentKey, charge, materialList, whId)
        }.build().asLiveData()
    }

    override suspend fun getLotDetailsLocal(charge: String): LiveData<List<VegaEcuadorDispatchStocks>> = dao.getLotDetail(charge)

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaEcuadorDispatchStocks>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaEcuadorDispatchStocks>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getStockListOffline() = dao.getStocks()

    override suspend fun saveLots(dispatchLotsList: MutableList<VegaEcuadorDispatchLots>, model: VegaEcuadorDispatch) {
        //dao.insertDispatchDetail(model)
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun deleteLot(batchNumber: String, tmpId: String) {
        dao.deleteLot(batchNumber, tmpId)
    }

    override suspend fun insertLot(lot: VegaEcuadorDispatchLots) {
        lot.isAdded = true
        dao.saveLots(mutableListOf(lot))
    }

    override fun getDispatchLots(tmpId: String) = dao.getLots(tmpId)

    override suspend fun saveDispatch(dispatchData: VegaEcuadorDispatch) = dao.insertDispatchDetail(dispatchData)
    override suspend fun saveDispatchLotLineItems(lotList: ArrayList<VegaEcuadorDispatchLots>) =
        dao.saveDispatchLotLineItems(lotList)

    override suspend fun getDispatchWithLineItem(): LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        dao.getDispatchWithLineItem()

    override suspend fun getDispatchWithLineItemCount(): LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        dao.getDispatchWithLineItemCount()

    override suspend fun updateDeletedItem(tmpWbId: String) {
        dao.deleteDispatchItem(tmpWbId)
        dao.deleteDispatchLots(tmpWbId)
    }
}
