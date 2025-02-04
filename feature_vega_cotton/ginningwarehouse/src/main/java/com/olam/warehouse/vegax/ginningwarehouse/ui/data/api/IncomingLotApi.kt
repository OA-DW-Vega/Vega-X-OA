package com.olam.warehouse.vegax.ginningwarehouse.ui.data.api

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by SangiliPandian C on 05-03-2020.
 */

interface IncomingLotApi {

    @GET("x-ginning/warehouse/getLotDetails")
  suspend  fun getIncomingLots(@Query("key") key: String):
            GenericReqAndResp<List<IncomingLot>>

    @POST("x-ginning/warehouse/addToGinning")
    suspend fun sendLotForGinning(
        @Query("key") key: String,
        @Body incomingLot: IncomingLot
    ): GenericReqAndResp<GenericMessage>

}
