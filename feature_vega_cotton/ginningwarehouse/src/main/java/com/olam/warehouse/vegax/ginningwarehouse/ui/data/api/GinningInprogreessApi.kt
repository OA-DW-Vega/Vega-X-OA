package com.olam.warehouse.vegax.ginningwarehouse.ui.data.api

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GinningInprogress
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by SangiliPandian C on 16-03-2020.
 */

interface GinningInprogreessApi {

    @GET("x-ginning/warehouse/getGinningLot")
    suspend fun getLotDetails(@Query("key") key: String):
            GenericReqAndResp<GinningInprogress>

    @GET("x-ginning/warehouse/isBaleValid")
    suspend fun validateBale(@Query("key") key: String, @Query("baleID") baleId: String):
            GenericReqAndResp<GenericMessage>

    @POST("x-ginning/warehouse/saveGinningProcess")
    suspend fun saveGinning(
        @Query("key") key: String,
        @Body ginningInprogress: GinningInprogress
    ): GenericReqAndResp<GenericMessage>
}
