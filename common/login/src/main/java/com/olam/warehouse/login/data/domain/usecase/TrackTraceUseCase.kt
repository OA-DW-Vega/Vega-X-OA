package com.olam.warehouse.login.data.domain.usecase

import com.olam.warehouse.login.data.repository.TrackTraceRepository

class TrackTraceUseCase(private val repository: TrackTraceRepository) {
    suspend fun getSourceLotIdDetails(key: String, sourceLotId: String) =
        repository.getSourceLotDetails(key, sourceLotId)

    suspend fun getAllSourceLotIdList() = repository.getAllSourceLotIdList()
    suspend fun getAllTransactionIdList() = repository.getAllTransIdList()
    suspend fun getOfflineSourceLotDetails(sourceLotId: String) = repository.getOfflineSourceLotDetails(sourceLotId)

    suspend fun getSupplier(purChaseType: String?) = repository.getSuppliers(purChaseType)

    suspend fun getFarmerList() = repository.getFarmerList()

    suspend fun getFarmerEudrDetails(farmerData: String) = repository.getFarmerEudrDetails(farmerData)

    suspend fun getOnlineTransactionIdDetails(transactionId: String) =
        repository.getOnlineTransactionIdDetails(transactionId)

    suspend fun getOfflineTransactionIdDetails(transactionId: String) =
        repository.getOfflineTransactionIdDetails(transactionId)
}
