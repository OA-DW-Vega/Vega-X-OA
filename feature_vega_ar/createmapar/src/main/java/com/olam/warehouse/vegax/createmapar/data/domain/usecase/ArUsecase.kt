package com.olam.warehouse.vegax.createmapar.data.domain.usecase

import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.data.repo.ArRepository

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class ArUsecase(private val repo: ArRepository) {
    suspend fun saveLotDetails(lotDetails: ArLotDetails) = repo.saveLotDetails(lotDetails)
    suspend fun getLotDetails() = repo.getLotDetails()
}