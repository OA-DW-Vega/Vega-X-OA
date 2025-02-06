package com.olam.warehouse.vegax.ppqindiacoffee.data.domain.usecase

import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindiacoffee.data.repo.VegaIndiaCoffeePpqRepository
import java.util.*


class VegaIndiaCoffeePpqUsecase(val repo: VegaIndiaCoffeePpqRepository) {
    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)
    suspend fun saveInspectionLotDetails(lotDetail: VegaIndiaCoffeePpqInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)

    suspend fun getInventoryList() = repo.getInventoryDetails()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getCustomLocations() = repo.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
}
