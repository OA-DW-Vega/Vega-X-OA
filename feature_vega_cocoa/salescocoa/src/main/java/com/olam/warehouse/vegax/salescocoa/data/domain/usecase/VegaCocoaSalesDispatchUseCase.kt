package com.olam.warehouse.vegax.salescocoa.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPostRequest
import com.olam.warehouse.vegax.salescocoa.data.repo.VegaCocoaSalesRepository
import java.util.ArrayList


class VegaCocoaSalesDispatchUseCase(private val repository: VegaCocoaSalesRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getPurchaseOrder() = repository.getPurchaseOrder()
    suspend fun getQualityParams(charge: String, material: String, whId: String) =
        repository.getQualityParams(charge, material, whId)

    suspend fun deleteTruckAndLots(whId: String) = repository.deleteTruckAndLots(whId)
    suspend fun getProducts(code: String) = repository.getSingleProduct(code)
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = repository.getConfigItems(role)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun postAnticipatedDeliveryDetail(vegaDeliveryPost: VegaCocoaSalesPostRequest) =
        repository.postAnticipatedDeliveryDetail(vegaDeliveryPost)

    suspend fun getThirdPartyMaterials() = repository.getThirdPartyMaterials()

    suspend fun insertTruckInfo(dispatch: VegaCocoaSalesWB) = repository.insertTruckInfo(dispatch)
    suspend fun updateLotWeight(weight: String, batchNumber: String) = repository.updateLot(batchNumber, weight)
    suspend fun removeLotFromTruck(batchNumber: String) = repository.removeLot(batchNumber)
    suspend fun getBagItems(batchNumber: String) = repository.getBagItems(batchNumber)
    suspend fun saveDispatchAndLots(
        dispatchData: VegaCocoaSalesWB,
        dispatchLotsList: MutableList<VegaCocoaSalesLots>
    ) = repository.saveDispatchAndLots(dispatchData, dispatchLotsList)

    suspend fun getTruckDetails(whId: String): LiveData<VegaCocoaSalesWB> = repository.getWbInfo(whId)

    suspend fun getTruckDetailsForNoWieghScale(
        whId: String,
        salesOrder: String,
        salesType: String
    ): LiveData<VegaCocoaSalesWB> = repository.getWbInfoForNoWB(whId, salesOrder, salesType)

    suspend fun getLotsDetails(whId: String): List<VegaCocoaSalesLots> = repository.getLotsDetails(whId)

    suspend fun getLotsDetailsBySalesAndType(
        salesOrder: String,
        salesType: String
    ): List<VegaCocoaSalesLots> = repository.getLotsDetailsBySaleOrder(salesOrder, salesType)

    suspend fun validateLot(batchNumber: String): VegaCocoaSalesLots = repository.validateLot(batchNumber)

    suspend fun insertLot(whId: String, lot: VegaCocoaSalesLots) = repository.insertLot(whId, lot)

    suspend fun getPendingListLot(type: String) = repository.getPendingLotList(type)

    suspend fun getPendingLot(type: String) = repository.getPendingList(type)
    suspend fun deleteBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.deleteBagDetails(bagMaterial)

    suspend fun saveBagDetails(bagMaterial: VegaCocoaSweepingBagMaterial) = repository.saveBagDetails(bagMaterial)

    suspend fun updateSuccessData(
        wbId: VegaCocoaSalesWB,
        syncStatus: Boolean,
        status: Int,
        msg: String,
        lots: List<VegaCocoaSalesLots>
    ) =
        repository.updateSuccessStatus(wbId, syncStatus, status, msg, lots)

    suspend fun updateBagSuccessData(
        batchNumber: String,
        lots: List<VegaCocoaSweepingBagMaterial>
    ) =
        repository.updateBagSuccessStatus(batchNumber, lots)

    suspend fun updateLotEditWeight(lot: VegaCocoaSalesLots) = repository.updateLotEditWeight(lot)

    suspend fun getPalletDetails(batchNumber: String, material: String) =
        repository.getPalletDetails(batchNumber, material)

    suspend fun getSupplier() = repository.getSuppliers()

    suspend fun getStockList(materialList: ArrayList<String>) = repository.getStockList(materialList)
    suspend fun getAllProducts() = repository.getAllProduct()
}
