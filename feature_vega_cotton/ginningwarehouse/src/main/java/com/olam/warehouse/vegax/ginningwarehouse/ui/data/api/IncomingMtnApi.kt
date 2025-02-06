package com.olam.warehouse.vegax.ginningwarehouse.ui.data.api

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IncomingMtnApi {

    @GET("x-ginning/warehouse/getPWIncomingMTNList")
    suspend fun getMtnList(@Query("key") key: String): GenericReqAndResp<List<Mtn>>

    @POST("x-ginning/warehouse/getMtnBalesForGRN")
    suspend fun postMtnWithBales(
        @Query("key") key: String,
        @Body mtn: Mtn
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/warehouse/getMtnBalesForGRN")
    suspend fun postOfflineMtnWithBales(
        @Query("key") key: String,
        @Body mtn: Mtn
    ): Call<GenericReqAndResp<GenericMessage>>
}
