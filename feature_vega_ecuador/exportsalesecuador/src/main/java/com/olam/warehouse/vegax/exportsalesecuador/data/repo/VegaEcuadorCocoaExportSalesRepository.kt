package com.olam.warehouse.vegax.exportsalesecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeExportSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.exportsalesecuador.data.api.VegaEcuadorCocoaExportSalesApi
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.model.VegaCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.model.VegaCoffeeExportSalesPostRequest
import java.util.*

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
interface VegaCoffeeExportSalesRepository {
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>>
    suspend fun getOTWithContainer(otNumber: String): LiveData<VegaCoffeeExportOTWithContainer>
    suspend fun saveContainer(container: VegaCoffeeExportSalesContainer)
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>
    suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots
    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots>
    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer
    suspend fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>>
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun removeConatinerWitfLots(containerNumber: String)
    suspend fun updateContainerId(oldContainerId: String, newContainerId: String)
}

class VegaCoffeeExportSalesRepositoryImpl(
    private val api: VegaEcuadorCocoaExportSalesApi,
    private val dao: VegaCoffeeExportSalesDao
) : VegaCoffeeExportSalesRepository {

    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>> =
                api.getPurchaseOrder(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getOTWithContainer(otNumber: String) = dao.getOTWithContainer(otNumber)
    override suspend fun saveContainer(container: VegaCoffeeExportSalesContainer) {
        val ot = VegaCoffeeExportSalesOrder()
        ot.saleOrderId = container.saleOrderId
        dao.saveOT(ot)
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

    override suspend fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots> =
        dao.getContainerWithLots(containerNumber)

    override suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots) =
        dao.removeLotDetails(lot.batchNumber, lot.materialCode)
    override suspend fun validateContainer(containerNumber: String) = dao.validateContainer(containerNumber)
    override suspend fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>> = dao.getAddedLotList()
    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeExportSalesPostRequest> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun updateLotEditWeight(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        val isFailure = lots.any { !it.deliveryFlag!! || !it.containerFlag!! || !it.pickingFlag!! }
        lots.forEach { lot ->
            if (!isFailure) {
                dao.deleteSalesOrder(lot.saleOrderId)
                dao.deleteSalesConatainer(lot.saleOrderId)
                dao.deleteSalesContainerLots(lot.saleOrderId)
            } else
                dao.updateLotStatus(
                    lot.batchNumber,
                    lot.delivery.toString(),
                    lot.deliveryItem.toString(),
                    lot.weighBridgeId.toString(),
                    lot.deliveryFlag ?: false,
                    lot.pickingFlag ?: false,
                    lot.containerFlag ?: false,lot.tmpId.toString()

                )
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
}
