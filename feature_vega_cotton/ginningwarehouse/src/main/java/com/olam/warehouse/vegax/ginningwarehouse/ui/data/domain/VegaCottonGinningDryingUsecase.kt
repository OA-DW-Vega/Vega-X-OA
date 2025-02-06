package com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.DryingRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingMtnRepository

class VegaCottonGinningDryingUsecase  (private val repository: DryingRepository
) {
  suspend fun fetchDryingLots()=repository.fetchDryingLots()
    suspend fun postLotForGinning(mIncomingLot: List<IncomingLot>)= repository.postLotForGinning(mIncomingLot)
}
