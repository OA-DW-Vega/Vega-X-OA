package com.olam.warehouse.vegax.ginningwarehouse.ui.data.domain

import com.olam.warehouse.ginning.data.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.DryingRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.IncomingMtnRepository
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo.InventoryRepository

class VegaCottonGinningInventoryUseCase  (private val repository: InventoryRepository
) {
    suspend fun syncBaleByGrade(grade: String)=repository.syncBaleByGrade(grade)
    suspend fun getInventoryGrades()= repository.getInventoryGrades()
    suspend fun fetchInventoryBaleList()= repository.fetchInventoryBaleList()
}
