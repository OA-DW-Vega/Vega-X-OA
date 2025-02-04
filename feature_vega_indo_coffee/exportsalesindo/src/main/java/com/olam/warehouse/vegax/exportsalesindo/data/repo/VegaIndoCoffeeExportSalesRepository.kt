package com.olam.warehouse.vegax.exportsalesindo.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeExportSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.exportsalesindo.data.api.VegaIndoCoffeeExportSalesApi
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalesindo.utils.getTmpId
import com.olam.warehouse.vegax.exportsalesindo.utils.prepareSalesOrder

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
interface VegaIndoCoffeeExportSalesRepository {
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>>
    suspend fun getOTWithContainer(tmpId: String): LiveData<VegaIndoCoffeeExportOTWithContainer>
    suspend fun saveContainer(
        container: VegaCoffeeExportSalesContainer,
        currentSaleOrder: ArrayList<IndoExporSalesMaterialList>
    )

    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>
    suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots
    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun getContainerWithLots(containerNumber: String): LiveData<IndoContainerWithLots>
    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer
    suspend fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaIndoCoffeeExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>>
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun removeConatinerWitfLots(containerNumber: String)
    suspend fun updateContainerId(oldContainerId: String, newContainerId: String)
    suspend fun getOfflinePurchaseOrder(): LiveData<List<IndoExporSalesMaterialList>>
    suspend fun getStockListOffline(): LiveData<List<VegaEcuadorDispatchStocks>>
    suspend fun getIndoExportSalesItem(): LiveData<List<VegaIndoCoffeeExportSalesOrder>>
    suspend fun deletedItem(tmpId: String)
}

class VegaIndoCoffeeExportSalesRepositoryImpl(
    private val api: VegaIndoCoffeeExportSalesApi,
    private val dao: VegaCoffeeExportSalesDao
) : VegaIndoCoffeeExportSalesRepository {

    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>> =
                api.getPurchaseOrder(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getOTWithContainer(tmpId: String) = dao.getOTWithContainerIndo(tmpId)
    override suspend fun saveContainer(
        container: VegaCoffeeExportSalesContainer,
        currentSaleOrder: ArrayList<IndoExporSalesMaterialList>
    ) {
        val otList = prepareSalesOrder(currentSaleOrder)
        val ot = if (otList.isNotEmpty()) otList[0] else VegaIndoCoffeeExportSalesOrder()
        val tmpId = if (container.tmpId?.isEmpty() == true) getTmpId() else container.tmpId
        ot.saleOrderId = container.saleOrderId
        ot.tmpId = tmpId.toString()
        ot.erdat = DateUtils.getCurrentTimeInMills().toString()
        container.tmpId = tmpId
        dao.saveIndoOT(ot)
        dao.saveContainer(container)
    }

    override suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeExportSalesLots>> =
                api.getStockList(getCurrentKey(), materialList)
        }.build().asLiveData()
    }

    override suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeExportSalesLots>> =
                api.getLot(getCurrentKey(), charge, materialList, whId)

        }.build().asLiveData()
    }

    override suspend fun validateLot(whId: String) = dao.validateLotAlreadyAdded(whId)
    override suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) {
        dao.saveLotDetails(vegaCoffeeSalesLots)
    }

    override suspend fun getContainerWithLots(containerNumber: String): LiveData<IndoContainerWithLots> =
        dao.getContainerWithLotsIndo(containerNumber)

    override suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots) =
        dao.removeLotDetails(lot.batchNumber, lot.materialCode)

    override suspend fun validateContainer(containerNumber: String) = dao.validateContainer(containerNumber)
    override suspend fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>> = dao.getAddedLotList()
    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaIndoCoffeeExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeeExportSalesPostRequest> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun updateLotEditWeight(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        val isFailure = lots.any { !it.deliveryFlag!! || !it.containerFlag!! || !it.pickingFlag!! }
        lots.forEach { lot ->
            if (lot.batchNumber.isEmpty()) {
                if (lot.synStatusMsg?.isNotEmpty() == true) dao.updateSalesOrderStatus(
                    lot.tmpId,
                    lot.synStatusMsg,
                    3,
                    false,
                    false,
                    false,
                    false
                )
                else dao.updateSalesOrderOfflineStatus(lot.tmpId)
            } else {
                if (!isFailure) {
                    /* dao.deleteSalesOrder(lot.saleOrderId)
                dao.deleteSalesConatainer(lot.saleOrderId)
                dao.deleteSalesContainerLots(lot.saleOrderId)*/
                    dao.updateSalesOrderStatus(
                        lot.tmpId,
                        lot.synStatusMsg,
                        4,
                        lot.deliveryFlag ?: false,
                        lot.pickingFlag ?: false,
                        lot.containerFlag ?: false,
                        true
                    )
                    dao.updateLotStatus(
                        lot.batchNumber,
                        lot.delivery.toString(),
                        lot.deliveryItem.toString(),
                        lot.weighBridgeId.toString(),
                        lot.deliveryFlag ?: false,
                        lot.pickingFlag ?: false,
                        lot.containerFlag ?: false,
                        lot.tmpId.toString()
                    )
                } else {
                    dao.updateSalesOrderStatus(
                        lot.tmpId,
                        lot.synStatusMsg,
                        3,
                        lot.deliveryFlag ?: false,
                        lot.pickingFlag ?: false,
                        lot.containerFlag ?: false,
                        false
                    )
                    dao.updateLotStatus(
                        lot.batchNumber,
                        lot.delivery.toString(),
                        lot.deliveryItem.toString(),
                        lot.weighBridgeId.toString(),
                        lot.deliveryFlag ?: false,
                        lot.pickingFlag ?: false,
                        lot.containerFlag ?: false,
                        lot.tmpId.toString()
                    )
                }
            }
        }
    }

    override suspend fun removeConatinerWitfLots(containerNumber: String) {
        dao.deleteLots(containerNumber)
        dao.deleteContainer(containerNumber)
    }

    override suspend fun updateContainerId(oldContainerId: String, newContainerId: String) {
        dao.updateContainer(oldContainerId, newContainerId)
        dao.updateLot(oldContainerId, newContainerId)
    }

    override suspend fun getOfflinePurchaseOrder() = dao.getOfflinePurchaseOrder()
    override suspend fun getStockListOffline() = dao.getStocks()
    override suspend fun getIndoExportSalesItem() = dao.getIndoExportSalesItem()
    override suspend fun deletedItem(tmpId: String) {
        dao.deleteIndoSalesOrder(tmpId)
        dao.deleteIndoSalesConatainer(tmpId)
        dao.deleteIndoSalesContainerLots(tmpId)
    }
}

