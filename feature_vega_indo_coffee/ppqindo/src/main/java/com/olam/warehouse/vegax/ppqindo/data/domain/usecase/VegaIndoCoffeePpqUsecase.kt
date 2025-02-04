package com.olam.warehouse.vegax.ppqindo.data.domain.usecase

import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindo.data.repo.VegaIndoCoffeePpqRepository

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeePpqUsecase(val repo: VegaIndoCoffeePpqRepository) {
    suspend fun getInspectionLots() = repo.getInspectionLots()
    suspend fun getInspectionLotDetails(lotId: String) = repo.getInspectionLotDetails(lotId)
    suspend fun saveInspectionLotDetails(lotDetail: VegaIndoCoffeePpqInspectionLotDetails) =
        repo.saveInspectionLotDetails(lotDetail)
}

