package com.olam.warehouse.vegax.ppqcameroon.data.domain.usecase

import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.repo.VegaCameroonPpqRepository

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCameroonPpqUsecase(val repo: VegaCameroonPpqRepository) {
    suspend fun getQualityParams(materialId: String, valueExist: Boolean?, wbId: String?) =
        repo.getQualityParams(materialId, valueExist, wbId)
    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)
    suspend fun saveInspectionLotDetails(lotDetail: VegaCameroonPpqInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)
}
