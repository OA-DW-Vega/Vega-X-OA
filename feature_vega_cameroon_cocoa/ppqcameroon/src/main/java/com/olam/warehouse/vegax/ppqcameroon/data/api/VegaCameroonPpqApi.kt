package com.olam.warehouse.vegax.ppqcameroon.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLots
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqPostResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaCameroonPpqApi {

    @GET("x-processing/quality/getInspectionLots")
    suspend fun getInspectionLots(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaCameroonPpqInspectionLots>>

    @GET("x-processing/quality/getInspectionLotDetails")
    suspend fun getInspectionLotDetails(
        @Query("key") key: String,
        @Query("inspectionLotId") lotId: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>

    @POST("x-processing/quality/saveInspectionLotDetails")
    suspend fun saveInspectionLotDetails(
        @Query("key") key: String,
        @Query("plantId") plant: String,
        @Body lotDetail: VegaCameroonPpqInspectionLotDetails
    ): GenericReqAndResp<VegaCameroonPpqPostResponse>
}
