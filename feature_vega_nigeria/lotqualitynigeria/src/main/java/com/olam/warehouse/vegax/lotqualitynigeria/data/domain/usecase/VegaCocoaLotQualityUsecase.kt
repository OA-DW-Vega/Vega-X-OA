package com.olam.warehouse.vegax.lotqualitynigeria.data.domain.usecase

import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.VegaCocoaLotQualityInspectionLotDetails
import com.olam.warehouse.vegax.lotqualitynigeria.data.repo.VegaCocoaLotQualityRepository
import java.util.*


class VegaCocoaLotQualityUsecase(val repo: VegaCocoaLotQualityRepository) {


    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)

    suspend fun getInventoryList() = repo.getInventoryDetails()
    suspend fun getProducts() = repo.getProducts()
    suspend fun getCustomLocations() = repo.getCustomLocations()
    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun saveQualityData(qualityParameter: VegaQuality, batchNo: String) =
        repo.saveQualityData(qualityParameter, batchNo)

    suspend fun saveWBDB(weighBridge: VegaQualityWBDetails) = repo.saveWBDB(weighBridge)
    suspend fun postQuality(qualityPost: VegaQualityPostLot) = repo.postQuality(qualityPost)
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun saveInspectionLotDetails(lotDetail: VegaCocoaLotQualityInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)

    suspend fun postNigeriaQuality(qualityPost: VegaQualityNigeriaLotPost) =
        repo.postNigeriaQuality(qualityPost)

    suspend fun getStockList(materialList: ArrayList<String>) = repo.getStockList(materialList)
    suspend fun updateDeletedItem(weighBridgeId: String) = repo.updateDeletedItem(weighBridgeId)
}
