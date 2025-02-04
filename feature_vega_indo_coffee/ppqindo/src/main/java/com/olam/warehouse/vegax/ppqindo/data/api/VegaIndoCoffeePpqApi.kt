package com.olam.warehouse.vegax.ppqindo.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindo.data.domain.model.VegaIndoCoffeePpqPostResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
interface VegaIndoCoffeePpqApi {
    @GET("x-processing/quality/getInspectionLots")
    suspend fun getInspectionLots(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaIndoCoffeePpqInspectionLots>>

    @GET("x-processing/quality/getInspectionLotDetails")
    suspend fun getInspectionLotDetails(
        @Query("key") key: String,
        @Query("inspectionLotId") lotId: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<VegaIndoCoffeePpqInspectionLotDetails>

    @POST("x-processing/quality/saveInspectionLotDetails")
    suspend fun saveInspectionLotDetails(
        @Query("key") key: String,
        @Query("plantId") plant: String,
        @Body lotDetail: VegaIndoCoffeePpqInspectionLotDetails
    ): GenericReqAndResp<VegaIndoCoffeePpqPostResponse>
}
