package com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain

import com.olam.warehouse.ginning.data.repo.GinningDispatchRepository
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingLotRepository

class VegaCottonIncomingLotsSeedCottonUseCase (private val repository: IncomingLotRepository
) {
    suspend fun fetchingIncomingLots() = repository.fetchingIncomingLots()

    suspend fun postLotForGinning(incomingLot: IncomingLot) = repository.postLotForGinning(incomingLot)

}
