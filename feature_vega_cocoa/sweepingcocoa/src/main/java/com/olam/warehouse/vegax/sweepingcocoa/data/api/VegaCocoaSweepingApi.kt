package com.olam.warehouse.vegax.sweepingcocoa.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaCocoaSweepingLots
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingPost
import com.olam.warehouse.vegax.sweepingcocoa.data.domain.model.VegaSweepingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCocoaSweepingApi {
    @GET("x-master/isValidLotID")
    suspend fun getLoTInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaSweepingLots>>

    @POST("x-processing/vega/processing/createStockDetails")
    suspend fun postSweeping(@Body vegaSweepingPost: VegaSweepingPost): GenericReqAndResp<VegaSweepingResponse>
}
