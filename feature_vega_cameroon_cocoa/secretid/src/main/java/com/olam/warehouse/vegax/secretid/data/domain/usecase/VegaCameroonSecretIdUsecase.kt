package com.olam.warehouse.vegax.secretid.data.domain.usecase

import com.olam.warehouse.vegax.secretid.data.repo.VegaCameroonSecretIdRepository


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCameroonSecretIdUsecase(val repo: VegaCameroonSecretIdRepository) {

    suspend fun getDashboardResult(startDate: String, endDate: String, isMtnt: Boolean) =
        repo.getDashboardResult(startDate, endDate, isMtnt)

    suspend fun getWbWeightDetails(wbid: String) = repo.getWbWeightDetails(wbid)

}
