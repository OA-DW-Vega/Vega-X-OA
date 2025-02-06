package com.olam.warehouse.vegax.exportsalescameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaStorageLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeExportSalesDao
import com.olam.warehouse.master.vegacoffee.entity.VegaCameroonExportSalesWithBagItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.master.veganicaragua.dao.VegaNicaraguaGrnDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.exportsalescameroon.data.api.VegaCameroonExportSalesApi
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.*
import java.util.*

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
interface VegaCameroonExportSalesRepository {
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonExportSalesOrderModel>>>>
    suspend fun getOTWithContainer(otNumber: String): LiveData<VegaCoffeeExportOTWithContainer>
    suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonInventoryModel>>>
    suspend fun saveContainer(container: VegaCoffeeExportSalesContainer)
    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>
    suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>>

    suspend fun validateLot(batchNumber: String): VegaCoffeeExportSalesLots
    suspend fun getBagItems(batchNumber: String, material: String): LiveData<List<VegaCocoaSweepingBagMaterial>>
    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial)
    suspend fun deleteBagDetails(id: Int)
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonExportSalesWeighScalePallet>>>>
    suspend fun getCustomLocations(): LiveData<List<VegaCustomStLocation>>

    suspend fun saveLotDetails(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun getContainerWithLots(containerNumber: String): LiveData<ContainerWithLots>
    suspend fun removeLotDetails(lot: VegaCoffeeExportSalesLots)
    suspend fun validateContainer(containerNumber: String): VegaCoffeeExportSalesContainer
    suspend fun getAddedLotList(): LiveData<List<VegaCoffeeExportSalesLots>>
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCameroonExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCameroonExportSalesPostRequest>>>
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeExportSalesLots>)
    suspend fun removeConatinerWitfLots(containerNumber: String)
    suspend fun updateContainerStatus(status:String,containerNumber: String): LiveData<Resource<GenericReqAndResp<VegaCameroonContainerStatusUpdate>>>
    suspend fun updateContainerId(oldContainerId: String, newContainerId: String)
    suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>>
    suspend fun getStoreLocations(): LiveData<List<VegaStorageLocation>>
    suspend fun getExportSalesPalletBags(batchNumber: String): LiveData<VegaCameroonExportSalesWithBagItems>
    suspend fun saveExportSales(lots: VegaCoffeeExportSalesLots)
    suspend fun getAllProduct(): LiveData<List<VegaMaterial>>
}

class VegaCameroonExportSalesRepositoryImpl(
    private val api: VegaCameroonExportSalesApi,
    private val dao: VegaCoffeeExportSalesDao,
    private val daoNic: VegaNicaraguaGrnDao
) : VegaCameroonExportSalesRepository {
    val werks = PreferenceHelper.get(Constants.WERKS, "")

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCameroonExportSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonExportSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonExportSalesOrderModel>> =
                api.getPurchaseOrder(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getBagItems(
        batchNumber: String,
        material: String
    ): LiveData<List<VegaCocoaSweepingBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber, material) else dao.getBagItems()
    }
    override suspend fun getCustomLocations() = dao.getCustomLocations()

    override suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = dao.saveBagDetails(bagMaterial)
    override suspend fun deleteBagDetails(id: Int) = dao.deleteBagDetails(id)

    override suspend fun getOTWithContainer(otNumber: String) = dao.getOTWithContainer(otNumber)
    override suspend fun getContainerInventory(status:String): LiveData<Resource<GenericReqAndResp<VegaCameroonInventoryModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonInventoryModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCameroonInventoryModel> =
                api.getContainerInventory(currentKey,status,werks)
        }.build().asLiveData()
    }

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCameroonExportSalesWeighScalePallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCameroonExportSalesWeighScalePallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCameroonExportSalesWeighScalePallet>> =
                api.getPalletDetails(batchNumber, currentKey, material)
        }.build().asLiveData()
    }

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
    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCameroonExportSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCameroonExportSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonExportSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCameroonExportSalesPostRequest> =
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
                dao.deleteContainerLotBagDetails(lot.batchNumber)
            } else
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

    override suspend fun removeConatinerWitfLots(containerNumber: String) {
        dao.deleteLots(containerNumber)
        dao.deleteContainer(containerNumber)
    }

    override suspend fun updateContainerStatus(status: String, containerNumber: String): LiveData<Resource<GenericReqAndResp<VegaCameroonContainerStatusUpdate>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCameroonContainerStatusUpdate>>() {
            var statusUpdate =
                VegaCameroonContainerUpdate(containerNumber, currentKey, werks, status)

            override suspend fun createCall(): GenericReqAndResp<VegaCameroonContainerStatusUpdate> =
                api.updateContainerStatus(statusUpdate)
        }.build().asLiveData()
    }

    override suspend fun updateContainerId(oldContainerId: String, newContainerId: String) {
        dao.updateContainer(oldContainerId, newContainerId)
        dao.updateLot(oldContainerId, newContainerId)
    }

    override suspend fun getShiftRemarks(role: String): LiveData<List<VegaCocoaMiscellaneous>> =
        dao.getShiftRemarkItems(role)

    override suspend fun getStoreLocations() = daoNic.getStorageLocations(getPlantDetails().plantId)

    override suspend fun getExportSalesPalletBags(batchNumber: String)= dao.getOfflineExportSalesPalletBags(batchNumber)
    override suspend fun saveExportSales(lots: VegaCoffeeExportSalesLots)= dao.saveExportDetails(lots.palletWeight?:"",lots.palletAvg?:"",lots.palletCount?:"",lots.batchNumber)
    override suspend fun getAllProduct(): LiveData<List<VegaMaterial>> = dao.getAllProducts()
}
