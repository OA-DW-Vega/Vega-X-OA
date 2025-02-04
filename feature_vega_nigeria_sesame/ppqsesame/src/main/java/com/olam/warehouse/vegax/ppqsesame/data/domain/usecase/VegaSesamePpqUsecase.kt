package com.olam.warehouse.vegax.ppqsesame.data.domain.usecase

import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqsesame.data.repo.VegaSesamePpqRepository
import java.util.*

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaSesamePpqUsecase(val repo: VegaSesamePpqRepository) {
    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)
    suspend fun saveInspectionLotDetails(lotDetail: VegaSesamePpqInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)

    suspend fun getInventoryList() = repo.getInventoryDetails()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getCustomLocations() = repo.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)

    suspend fun getStoreLocations() = repo.getStoreLocations()
}
