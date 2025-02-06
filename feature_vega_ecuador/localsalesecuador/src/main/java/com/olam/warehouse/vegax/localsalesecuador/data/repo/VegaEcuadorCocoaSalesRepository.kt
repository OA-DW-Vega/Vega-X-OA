package com.olam.warehouse.vegax.localsalesecuador.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.dao.VegaCocoaDispatchDao
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
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
import com.olam.warehouse.vegax.localsalesecuador.data.api.VegaEcuadorCocoaSalesApi
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesOrderModel
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPallet
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPostRequest
import java.util.*

interface VegaCoffeeSalesRepository {
    suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>>
    suspend fun getWBPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>
    suspend fun getDispatchSalesItem(
        soNumber: String,
        salesType: String,
        salesTempId: String
    ): LiveData<VegaCoffeeSalesOrderWithLots>

    suspend fun getStockList(materialList: ArrayList<String>): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>
    suspend fun saveDispatchAndLots(salesOrder: VegaCoffeeSalesOrder, lots: ArrayList<VegaCoffeeSalesLots>)
    suspend fun validateLot(batchNumber: String): VegaCoffeeSalesLots
    suspend fun getQualityParams(
        charge: String,
        materialList: List<String>, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>>

    suspend fun removeLotDetails(lot: VegaCoffeeSalesLots)
    suspend fun getBagItems(batchNumber: String, materialCode: String): LiveData<List<VegaCoffeeSalesBagMaterial>>
    suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>>

    suspend fun saveBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial)
    suspend fun deleteBagDetails(bagMaterial: VegaCoffeeSalesBagMaterial)
    suspend fun getPendingLotList(type: String): LiveData<List<VegaCoffeePendingSalesOrderWithLots>>
    suspend fun updateLotEditWeight(lot: ArrayList<VegaCoffeeSalesLots>)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>>
    suspend fun deleteTempData(salesTempId: String)
    suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>>

    suspend fun validateWBLot(batchNumber: String, material: String): VegaCocoaDispatchLots
    suspend fun removeLot(batchNumber: String, material: String)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaDispatchWB>
    suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>>
    suspend fun getThirdPartyMaterials(): List<VegaMaterial>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun vendorInfo(batchNumber: String): VegaVendor
    suspend fun deleteTruckAndLots(whId: String)
    suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>>

    suspend fun getSingleProduct(code: String): LiveData<VegaMaterial>

    suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaDispatchLots)
    suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>>
    suspend fun updateSuccessStatus(
        wbId: VegaCocoaDispatchWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    )

    suspend fun getPreSamplingQualityList(
        batchNo: String,
        materialId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>>
}

class VegaCoffeeSalesRepositoryImpl(
    private val api: VegaEcuadorCocoaSalesApi,
    private val dao: VegaCoffeeSalesDao,
    private val WBDao: VegaCocoaDispatchDao
) : VegaCoffeeSalesRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeSalesOrderModel>> =
                api.getPurchaseOrder(currentKey)
        }.build().asLiveData()
    }


    override suspend fun getWBPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaPurchaseOrder>> =
                api.getWBPurchaseOrder(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getPurchaseOrder(receivingWerks: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaPurchaseOrder>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaPurchaseOrder>> =
                api.getPurchaseOrder(
                    currentKey,
                    receivingWerks
                )
        }.build().asLiveData()
    }

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

    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getWBQuality(currentKey, charge, material, whId)

        }.build().asLiveData()
    }

    override suspend fun removeLotDetails(lot: VegaCoffeeSalesLots) =
        dao.removeLotDetails(lot.batchNumber, lot.salesTempId, lot.materialCode)

    override suspend fun getBagItems(
        batchNumber: String,
        materialCode: String
    ): LiveData<List<VegaCoffeeSalesBagMaterial>> {
        return if (batchNumber.isNotEmpty()) dao.getBagItems(batchNumber, materialCode) else dao.getBagItems()
    }

    override suspend fun getPalletDetails(
        batchNumber: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeeSalesPallet>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeeSalesPallet>> =
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

    override suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCoffeeSalesPostRequest): LiveData<Resource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeSalesPostRequest>>() {

            override suspend fun createCall(): GenericReqAndResp<VegaCoffeeSalesPostRequest> =
                api.postDeliveryDetail(vegaDeliveryPost)
        }.build().asLiveData()
    }

    override suspend fun deleteTempData(salesTempId: String) {
        dao.deleteSalesOrder(salesTempId)
        dao.deleteSalesLots(salesTempId)
        dao.deleteSalesLotsBagItems(salesTempId)
    }

    override suspend fun getTrucks(): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchWB>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchWB>> =
                api.fetchTruckList(currentKey, true)
        }.build().asLiveData()
    }

    override suspend fun validateWBLot(whId: String, purchaseOrder: String) =
        WBDao.validateLotAlreadyAdded(whId, purchaseOrder)

    override suspend fun deleteTruckAndLots(whId: String) {
        WBDao.removeLotsByWeighBridgeId(whId)
    }

    override suspend fun getStocksByMaterial(
        materialList: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaDispatchLots>> =
                api.getStockListByWB(currentKey, materialList)

        }.build().asLiveData()
    }

    override suspend fun getSingleProduct(code: String): LiveData<VegaMaterial> = WBDao.getSingleProducts(code)

    override suspend fun insertLot(weighBridgeId: String, lot: VegaCocoaDispatchLots) {
        lot.weighBridgeId = weighBridgeId
        lot.isAdded = true
        WBDao.saveLots(mutableListOf(lot))
    }

    override suspend fun removeLot(batchNumber: String, material: String) = WBDao.removeLots(batchNumber, material)

    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = WBDao.getConfigItems(role)

    override suspend fun getWbInfo(wbId: String): LiveData<VegaCocoaDispatchWB> = WBDao.getTruckData(wbId)

    override suspend fun getLotsDetails(whId: String): LiveData<List<VegaCocoaDispatchLots>> = WBDao.geLots(whId)

    override suspend fun getThirdPartyMaterials(): List<VegaMaterial> = WBDao.getThirdPartyMaterials()

    override suspend fun vendorInfo(batchNumber: String): VegaVendor = WBDao.vendorInfo(batchNumber)

    override suspend fun updateSuccessStatus(
        wbId: VegaCocoaDispatchWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaDispatchLots>
    ) {
        WBDao.updateDispatchStatus(wbId.weighBridgeId, syncStatus, status, msg)
        lots.forEach {
            it.isProgress = true
            WBDao.updateDispatchLotStatus(wbId.weighBridgeId, true)
        }
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
}
