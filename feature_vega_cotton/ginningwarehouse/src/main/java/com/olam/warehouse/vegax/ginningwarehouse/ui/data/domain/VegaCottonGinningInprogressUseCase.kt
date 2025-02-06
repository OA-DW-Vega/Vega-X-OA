package com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.GinningInprogressRepository

class VegaCottonGinningInprogressUseCase  (private val repository: GinningInprogressRepository
) {
    suspend fun fetchLotDetails()=repository.fetchLotDetails()
    suspend fun validateBale(baleId: String)=repository.validateBale(baleId)
    suspend fun saveGinning(inprogress: GinningInprogress)=repository.saveGinning(inprogress)
    suspend fun insertGInningBales(bale: Bale)=repository.insertBale(bale)
    suspend fun deleteGinningBale(bale: Bale)=repository.deleteGinningBale(bale)
    suspend fun deleteBalesByLotId(lotId: String)=repository.deleteBalesByLotId(lotId)
    suspend fun getBalesByLotNumber(lotId: String)=repository.getBalesByLotNumber(lotId)
}


