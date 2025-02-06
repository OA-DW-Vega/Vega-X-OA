package com.olam.warehouse.vegax.ppqcoffee.data.domain.usecase

import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcoffee.data.repo.VegaCoffeePpqRepository

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCoffeePpqUsecase(val repo: VegaCoffeePpqRepository) {
    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)
    suspend fun saveInspectionLotDetails(lotDetail: VegaCoffeePpqInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)
}

