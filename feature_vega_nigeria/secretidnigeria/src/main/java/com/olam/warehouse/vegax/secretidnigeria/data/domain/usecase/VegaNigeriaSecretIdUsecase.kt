package com.olam.warehouse.vegax.secretidnigeria.data.domain.usecase

import com.olam.warehouse.vegax.secretidnigeria.data.repo.VegaNigeriaSecretIdRepository


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaNigeriaSecretIdUsecase(val repo: VegaNigeriaSecretIdRepository) {

    suspend fun getDashboardResult(startDate: String, endDate: String, isMtnt: Boolean) =
        repo.getDashboardResult(startDate, endDate, isMtnt)

    suspend fun getWbWeightDetails(wbid: String) = repo.getWbWeightDetails(wbid)
    suspend fun getfetchWBListforMultiPlants(
        isMTNT: Boolean,
        startDate: String,
        endDate: String,
        plantList: List<String>
    ) = repo.getfetchWBListforMultiPlants(isMTNT, startDate, endDate, plantList)

}
