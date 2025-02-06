package com.olam.warehouse.vegax.salescocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaSalesDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.salescocoa.data.api.VegaCocoaSalesApi
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesDeliveryDetail
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesOrderModel
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPallet
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPostRequest
import java.util.*

interface VegaCocoaSalesRepository {
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>>
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>>

    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>>

    suspend fun getSingleProduct(code: String): LiveData<VegaMaterial>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>>
    suspend fun postAnticipatedDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>>

    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaSalesWB,
        dispatchLotsList: MutableList<VegaCocoaSalesLots>
    )

    suspend fun getThirdPartyMaterials(): List<VegaMaterial>
    suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaSalesLots)
    suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaSalesWB>
    suspend fun getWbInfoForNoWB(wbId: String, salesOrder: String, salesType: String): LiveData<VegaCocoaSalesWB>
    suspend fun updateLot(batchNumber: String, weight: String)
    suspend fun removeLot(batchNumber: String)
    suspend fun insertTruckInfo(dispatchData: VegaCocoaSalesWB)
    suspend fun getLotsDetails(whId: String): List<VegaCocoaSalesLots>
    suspend fun getLotsDetailsBySaleOrder(
        salesOrder: String,
        salesType: String
    ): List<VegaCocoaSalesLots>

    suspend fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun validateLot(batchNumber: String): VegaCocoaSalesLots
    suspend fun deleteTruckAndLots(whId: String)
    suspend fun getPendingLotList(type: String): LiveData<List<VegaCocoaSalesWB>>
    suspend fun getPendingList(type: String): List<VegaCocoaSalesWB>
    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)

    suspend fun updateSuccessStatus(
        wbId: VegaCocoaSalesWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaSalesLots>
    )

    suspend fun updateBagSuccessStatus(batchNumber: String, lots: List<VegaCocoaSweepingBagMaterial>)

    suspend fun updateLotEditWeight(lot: VegaCocoaSalesLots)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>>

    suspend fun getSuppliers(): LiveData<List<VegaVendor>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>>
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
}

class VegaCocoaSalesRepositoryImpl(
    private val api: VegaCocoaSalesApi,
    private val dao: VegaCocoaSalesDao,
    private val masterDao: MasterDao
) : VegaCocoaSalesRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesWB>> =
                api.fetchTruckList(currentKey, true/*, true*/)
        }.build().asLiveData()
    }

    override suspend fun getThirdPartyMaterials(): List<VegaMaterial> =
        dao.getThirdPartyMaterials()

    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesOrderModel>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesLots>> =
                api.getQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun getSingleProduct(code: String): LiveData<VegaMaterial> = dao.getSingleProducts(code)
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaDeliveryPostResponse>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaDeliveryPostResponse>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaDeliveryPostResponse> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun postAnticipatedDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>> =
                api.postAnticipatedDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaSalesWB,
        dispatchLotsList: MutableList<VegaCocoaSalesLots>
    ) {
        dao.insertDispatchTruckDetail(dispatchData)
        dispatchLotsList.forEach {
            it.weighBridgeId = dispatchData.weighBridgeId
            it.isAdded = true
        }
        dao.saveLots(dispatchLotsList)
    }

    override suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaSalesLots) {
        lot.weighBridgeId = weighBridgeId
        lot.isAdded = true
        dao.saveLots(mutableListOf(lot))
    }

    override suspend fun getWbInfo(wbId: String) = dao.getTruckData(wbId)

    override suspend fun getWbInfoForNoWB(
        wbId: String,
        salesOrder: String,
        salesType: String
    ): LiveData<VegaCocoaSalesWB> = dao.getTruckDataForNoWB(wbId, salesOrder, salesType)

    override suspend fun updateLot(batchNumber: String, weight: String) = dao.updateLotWeight(batchNumber, weight)

    override suspend fun removeLot(batchNumber: String) = dao.removeLots(batchNumber)

    override suspend fun insertTruckInfo(dispatchData: VegaCocoaSalesWB) =
        dao.insertDispatchTruckDetail(dispatchData)

    override suspend fun getLotsDetails(whId: String) = dao.geLots(whId)

    override suspend fun getLotsDetailsBySaleOrder(
        salesOrder: String,
        salesType: String
    ): List<VegaCocoaSalesLots> = dao.geLotsBySalesOrder(salesOrder, salesType)

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)
    override suspend fun deleteTruckAndLots(whId: String) {
        dao.deleteVegaDispatchTrucks(whId)
        dao.removeLotsByWeighBridgeId(whId)
    }

    override suspend fun getPendingLotList(type: String): LiveData<List<VegaCocoaSalesWB>> = dao.getPendingLotList(type)

    override suspend fun getPendingList(type: String): List<VegaCocoaSalesWB> = dao.getPendingList(type)

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)

    override suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) =
        dao.deleteBagDetails(bagMaterial.id, bagMaterial.batchNumber)

    override suspend fun getBagItems(batchNumber: String): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber) else dao.getBagItems()
    }

    override suspend fun updateSuccessStatus(
        wbId: VegaCocoaSalesWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaSalesLots>
    ) {
        dao.updateDispatchSalesStatus(wbId.weighBridgeId, syncStatus, status, msg)
        lots.forEach {
            it.isProgress = true
            dao.updateDispatchSalesLotStatus(wbId.weighBridgeId, true, it.salesOrderId, wbId.salesType)
        }
    }


    override suspend fun updateBagSuccessStatus(
        batchNumber: String,
        lots: List<VegaCocoaSweepingBagMaterial>
    ) {

        lots.forEach {
            dao.updateSalesBagSyncStatus(batchNumber, it.id)
        }
    }


    override suspend fun updateLotEditWeight(lot: VegaCocoaSalesLots) {
        dao.updateLotWeight(
            lot.batchNumber,
            lot.salesOrderId,
            lot.salesType ?: "",
            lot.editedWeight ?: "",
            lot.weightToDispatchUOM ?: ""
        )
    }

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesPallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesPallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun getSuppliers() = dao.getSuppliers()

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaSalesLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaSalesLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }
    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()
}
