package com.olam.warehouse.vegax.portwarehouse.data.domain

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.vegax.portwarehouse.data.repository.PortPileRepository

class VegaCottonPortWareHousePileUseCase (private val repository: PortPileRepository
) {
    suspend fun getStorageLocationList() = repository.getStorageLocationList()
    suspend fun validateBale(baleId: String, locationId: String) = repository.validateBale(baleId, locationId)
    suspend fun insertBale(bale: PortPileBale) = repository.insertBale(bale)
    suspend fun getBaleByLocationId(id: String) = repository.getBaleByLocationId(id)
    suspend fun getBaleByLocationIdOffline(id: String) = repository.getBaleByLocationIdOffline(id)
    suspend fun isAlreadyExistBale(baleId: String) = repository.isAlreadyExistBale(baleId)
    suspend fun deleteBalesDB(pileId: String) = repository.deleteBalesDB(pileId)
    suspend fun removeBale(bale: PortPileBale) = repository.removeBale(bale)
    suspend fun postPile(mCurrentPiles: PortPileStorageLocationModel) = repository.postPile(mCurrentPiles)
}
