package com.olam.warehouse.vegax.localsalesnigeria.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeePendingSalesOrderWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.localsalesnigeria.data.api.VegaNigeriaSalesApi
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesOrderModel
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesPallet
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesPostRequest
import java.util.*

interface VegaCoffeeSalesRepository {
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>>
    suspend fun getDispatchSalesItem(
        soNumber: String,
        salesType: String,
        salesTempId: String
    ): LiveData<VegaCoffeeSalesOrderWithLots>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>
    suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>)
    suspend fun validateLot(batchNumber: String): VegaCoffeeSalesLots
    suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>

    suspend fun removeLotDetails(lot: VegaCoffeeSalesLots)
    suspend fun getBagItems(batchNumber: String, materialCode: String, salesTempId: String): LiveData<List<VegaCoffeeSalesBagMaterial>>
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>>

    suspend fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial)
    suspend fun getPendingLotList(type: String): LiveData<List<VegaCoffeePendingSalesOrderWithLots>>
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeSalesLots>)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>>
    suspend fun deleteTempData(salesTempId: String)

}

class VegaCoffeeSalesRepositoryImpl(
    private val api: VegaNigeriaSalesApi,
    private val dao: VegaCoffeeSalesDao
) : VegaCoffeeSalesRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaSalesOrderModel>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun getDispatchSalesItem(
        soNumber: String,
        salesType: String,
        salesTempId: String
    ): LiveData<VegaCoffeeSalesOrderWithLots> = dao.getDispatchSalesItem(soNumber, salesType)

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeSalesLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>) {
        dao.insertSalesOrderDetail(salesOrder)
        lots.forEach {
            it.isAdded = true
        }
        dao.saveLots(lots)
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)

    override suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeSalesLots>> =
                api.getLot(currentKey, charge, materialList, whId)

        }.build().asLiveData()
    }

    override suspend fun removeLotDetails(lot: VegaCoffeeSalesLots) =
        dao.removeLotDetails(lot.batchNumber, lot.salesTempId, lot.materialCode)

    override suspend fun getBagItems(
        batchNumber: String,
        materialCode: String,
        salesTempId: String
    ): LiveData<List<VegaCoffeeSalesBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getCameroonSalesBagItems(batchNumber, materialCode, salesTempId) else dao.getBagItems()
    }

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaSalesPallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaSalesPallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

    override suspend fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) = dao.saveBagDetails(bagMaterial)
    override suspend fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial) =
        dao.deleteBagDetails(bagMaterial.id, bagMaterial.batchNumber)

    override suspend fun getPendingLotList(type: String): LiveData<List<VegaCoffeePendingSalesOrderWithLots>> =
        dao.getPendingLotList(type)

    override suspend fun updateLotEditWeight(lots: ArrayList<VegaCoffeeSalesLots>) {
        val isEndLot = lots.any { it.endLotFlag ?: false }
        val isFailure =
            if (isEndLot) lots.any { !it.deliveryFlag!! || !it.pickingFlag!! || !it.pgiFlag!! || !it.storageLossFlag!! }
            else lots.any { !it.deliveryFlag!! || !it.pickingFlag!! || !it.pgiFlag!! }
        lots.forEach { lot ->
            if (!isFailure) {
                dao.deleteSalesOrder(lot.salesTempId)
                dao.deleteSalesLots(lot.salesTempId)
                dao.deleteSalesLotsBagItems(lot.salesTempId)
            } else
                dao.updateLotStatus(
                    lot.batchNumber,
                    lot.delivery.toString(),
                    lot.deliveryItem.toString(),
                    lot.weighBridgeId.toString(),
                    lot.deliveryFlag ?: false,
                    lot.pgiFlag ?: false,
                    lot.pickingFlag ?: false,
                    lot.storageLossFlag ?: false
                )
        }
    }

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaNigeriaSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNigeriaSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaNigeriaSalesPostRequest> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun deleteTempData(salesTempId: String) {
        dao.deleteSalesOrder(salesTempId)
        dao.deleteSalesLots(salesTempId)
        dao.deleteSalesLotsBagItems(salesTempId)
    }

}
