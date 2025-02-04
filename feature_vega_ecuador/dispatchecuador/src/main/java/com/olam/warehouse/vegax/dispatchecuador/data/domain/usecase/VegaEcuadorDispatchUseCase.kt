package com.olam.warehouse.vegax.dispatchecuador.data.domain.usecase

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchWithLineItems
import com.olam.warehouse.vegax.dispatchecuador.data.repo.VegaEcuadorDispatchRepository

/**
 * Created by Keerthi Santhanam on 7/14/2020.
 */
class VegaEcuadorDispatchUseCase(private val repository: VegaEcuadorDispatchRepository) {
    suspend fun getPurchaseOrder(plantId: String) = repository.getPurchaseOrder(plantId)
    suspend fun getPurchaseOrderLocal(plantId: String) = repository.getPurchaseOrderLocal(plantId)
    suspend fun getProducts() = repository.getProducts()
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaEcuadorDeliveryPost) =
        repository.postDeliveryDetail(vegaDeliveryPost)

    suspend fun validateLot(batchNumber: String, tmpId: String): VegaEcuadorDispatchLots = repository.validateLot(batchNumber, tmpId)
    suspend fun updateRemark(remark: String, batchNumber: String) = repository.updateRemarks(remark, batchNumber)
    suspend fun getLotDetails(charge: String, materialList: ArrayList<String>, whId: String) =
        repository.getLotDetails(charge, materialList, whId)

    suspend fun getLotDetailsLocal(charge: String) = repository.getLotDetailsLocal(charge)
    suspend fun getStockList(materialList: ArrayList<String>) = repository.getStockList(materialList)
    suspend fun getStockListOffline() = repository.getStockListOffline()
    suspend fun saveLotInDispatch(list: MutableList<VegaEcuadorDispatchLots>, model: VegaEcuadorDispatch) =
        repository.saveLots(list, model)
    suspend fun deleteLot(batchNo: String, tmpId: String) = repository.deleteLot(batchNo, tmpId)
    suspend fun insertLot(lot: VegaEcuadorDispatchLots) = repository.insertLot(lot)
    fun getLots(tmpId: String) = repository.getDispatchLots(tmpId)
    suspend fun saveDispatch(dispatchData: VegaEcuadorDispatch) = repository.saveDispatch(dispatchData)
    suspend fun saveDispatchLotLineItems(lotList: ArrayList<VegaEcuadorDispatchLots>) =
        repository.saveDispatchLotLineItems(lotList)

    suspend fun getDispatchWithLineItem(): LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        repository.getDispatchWithLineItem()

    suspend fun getDispatchWithLineItemCount(): LiveData<List<VegaEcuadorDispatchWithLineItems>> =
        repository.getDispatchWithLineItemCount()

    suspend fun updateDeletedItem(tmpWbId: String) = repository.updateDeletedItem(tmpWbId)
}
