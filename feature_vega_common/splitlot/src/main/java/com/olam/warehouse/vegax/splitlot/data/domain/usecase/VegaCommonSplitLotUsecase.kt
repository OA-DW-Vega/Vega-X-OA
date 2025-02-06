package com.olam.warehouse.vegax.splitlot.data.domain.usecase

import com.olam.warehouse.vegax.splitlot.data.domain.model.SplitUpdateTallySequencePost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.splitlot.data.domain.model.VegaCommonSplitMainModel
import com.olam.warehouse.vegax.splitlot.data.repo.VegaCommonSplitLotRepo

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
class VegaCommonSplitLotUsecase(private val repo: VegaCommonSplitLotRepo) {
    suspend fun getLotQualityDetails(batchNo: String, materialId: String)  = repo.getLotQualityDetails(batchNo, materialId)
    suspend fun getMaterialQualityGrades(materialCode: String) = repo.getMaterialQualityGrades(materialCode)
    suspend fun getGrades(materialCode: String) = repo.getGrades(materialCode)
    suspend fun postSplitDeatils(postData: VegaCommonSplitMainModel)  =  repo.postSplitDeatils(postData)
    suspend fun postQuality(paramPost: VegaCoffeeQualityParamPost) = repo.postQuality(paramPost)
    suspend fun updateTallySequence(paramPost: SplitUpdateTallySequencePost) = repo.updateTallySequence(paramPost)
    suspend fun getQualityParams(materialId: String) = repo.getQualityParams(materialId)

}
