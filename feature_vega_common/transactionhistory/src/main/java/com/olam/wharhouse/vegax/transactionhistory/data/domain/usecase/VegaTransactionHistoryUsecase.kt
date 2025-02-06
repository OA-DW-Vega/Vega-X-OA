package com.olam.wharhouse.vegax.transactionhistory.data.domain.usecase

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
class VegaTransactionHistoryUsecase(val repository: com.olam.wharhouse.vegax.transactionhistory.data.repo.VegaTransHisRepo)
{

    suspend fun getHistoryTranxMtnt(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxMtnt(endDate, plant, startDate)
    suspend fun getHistoryTranxMtnr(endDate: String, plant: String, startDate: String) =
        repository.getHistoryTranxMtnr(endDate, plant, startDate)
    suspend fun getHistoryTranxGrn(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxGrn(endDate,plant,startDate)
    suspend fun getHistoryTranxFGrn(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxFgrn(endDate,plant,startDate)

    suspend fun getHistoryTranxRmin(endDate: String,plant: String,startDate: String) =
        repository.getHistoryTranxRmin(endDate,plant,startDate)


    suspend fun getSupplier() = repository.getSuppliers()
    suspend fun getuomDetail() = repository.getuomDetail()

    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String,PlantId: String ) =
        repository.getPreSamplingQualityList(batchNo, materialId,PlantId)

    suspend fun getQualityParams(materialId: String) = repository.getQualityParams(materialId)
}
