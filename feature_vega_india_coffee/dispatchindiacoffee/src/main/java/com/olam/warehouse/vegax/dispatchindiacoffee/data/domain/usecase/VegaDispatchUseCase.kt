package com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.usecase

import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDeliveryPost
import com.olam.warehouse.vegax.dispatchindiacoffee.data.repo.VegaDispatchRepository

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
class VegaDispatchUseCase(private val repository: VegaDispatchRepository) {
    suspend fun getTrucks() = repository.getTrucks()
    suspend fun getCustomLocations() = repository.getCustomLocations()
    suspend fun getStocks(material: String) = repository.getStocks(material)
    suspend fun getStocksOffline(material: String) = repository.getStocksOffline(material)
    suspend fun postDeliveryDetail(vegaDeliveryPost: VegaDeliveryPost) = repository.postDeliveryDetail(vegaDeliveryPost)
    suspend fun getQualityParams(charge: String, material: String) = repository.getQualityParams(charge, material)
    suspend fun getDelivery(delivery: String, deliveryItem: String) = repository.getDelivery(delivery, deliveryItem)
    suspend fun saveDispatchAndLots(dispatchData: VegaDispatchTrucks, dispatchLotsList: MutableList<VegaDispatchLots>) =
        repository.saveDispatchAndLots(dispatchData, dispatchLotsList)

    suspend fun updateLot(batchNumber: String) = repository.updateLot(batchNumber)
    suspend fun updateDispatchStatus(
        weighBridgeId: String,
        syncStatus: Boolean,
        status: Int,
        msg: String?,
        batch: String
    ) =
        repository.updateDispatchStatus(weighBridgeId, syncStatus, status, msg, batch)
}
