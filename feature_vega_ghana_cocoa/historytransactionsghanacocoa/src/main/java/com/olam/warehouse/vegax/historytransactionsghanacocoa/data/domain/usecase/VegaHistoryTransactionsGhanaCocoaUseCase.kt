package com.olam.warehouse.vegax.historytransactionsghanacocoa.data.domain.usecase

import com.olam.warehouse.vegax.historytransactionsghanacocoa.data.repo.VegaHistoryTransactionsGhanaCocoaRepository

class VegaHistoryTransactionsGhanaCocoaUseCase(private val repository: VegaHistoryTransactionsGhanaCocoaRepository) {

    suspend fun getHistoryTranxMtnt(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxMtnt(endDate, plant, startDate)
    suspend fun getHistoryTranxMtnr(endDate: String, plant: String, startDate: String) =
        repository.getHistoryTranxMtnr(endDate, plant, startDate)
    suspend fun getHistoryTranxGrn(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxGrn(endDate,plant,startDate)
    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getuomDetail() = repository.getuomDetail()
}
