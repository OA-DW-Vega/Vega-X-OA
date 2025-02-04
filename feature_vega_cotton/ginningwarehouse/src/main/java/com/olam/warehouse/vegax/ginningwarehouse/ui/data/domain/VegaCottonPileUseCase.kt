package com.olam.warehouse.vegax.ginningwarehouse.data.domain

import com.olam.warehouse.ginning.data.repo.GinningDispatchRepository
import com.olam.warehouse.ginning.ui.pile.db.entity.GinningPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.GinningPileRepository

class VegaCottonPileUseCase
    (private val repository: GinningPileRepository
) {
      suspend fun validateBale(baleId: String,storageLoctaion: String)=repository.validateBale(baleId,storageLoctaion)
      suspend fun getPileLocations() = repository.getPileLocations()
      suspend fun insertBaleToStorage(bale: List<PileBale>)=repository.insertBaleToStorage(bale)
    suspend fun getBaleListByStorageId(id: String) = repository.getBaleListByStorageId(id)
    suspend fun  removeBaleFromStorage(bale: PileBale)=repository.removeBaleFromStorage(bale)
    suspend fun getExistedBaleList()=repository.getExistedBaleList()
    suspend fun deleteBalesDB(pileId: String)=repository.deleteBalesDB(pileId)
    suspend fun postPile(mCurrentPiles: GinningPileStorageLocationModel)=repository.postPile(mCurrentPiles)
}
