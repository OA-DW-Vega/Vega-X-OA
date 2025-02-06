package com.olam.warehouse.vegax.qualityofanylot.data.domain.usecase

import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.data.repo.VegaAnyLotQualityRepository
import java.util.ArrayList

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

class VegaAnyLotQualityUsecase (val repo: VegaAnyLotQualityRepository) {
    suspend fun getCustomLocations() = repo.getCustomLocations()

    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)

    suspend fun getAllStockByPlant()= repo.getAllStockByPlant()

    suspend fun getQualityLot(plant:String,batchNo: String)= repo.getSavedQualityLotList(plant,batchNo)

    suspend fun saveOrPostQualityDetails(request: VegaAnyLotQualityPostRequest)= repo.saveOrPostQuality(request)

    suspend fun deleteTransactionItem(id: String)= repo.deleteTransaction(id)

    suspend fun getQualityId(key:String,id:String)= repo.getQualityById(key, id)

    suspend fun getPreSamplingQualityList(batchNo: String, materialId: String) =
        repo.getPreSamplingQualityList(batchNo, materialId)

    suspend fun getValidLotsList(charge: String, material: String, whId: String) =
        repo.getValidLots(charge, material, whId)

    suspend fun postBatchQuality(qualityPost: VegaQualityNigeriaPost) =
        repo.postBatchQuality(qualityPost)

}
