package com.olam.warehouse.vegax.portwarehouse.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.portwarehouse.data.model.Deliverylist
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface IncomingApi {

    @POST("x-ginning/port/incomingLot/getPWMTNDetailsList")
    suspend fun getMtnList(
        @Query("key") key: String,
        @Body baleList: Deliverylist
    ): GenericReqAndResp<List<PortMtn>>

    @POST("x-ginning/port/incomingLot/getMtnBalesForGRN")
    suspend fun postMtnWithBales(@Query("key") key: String, @Body mtn: PortMtn): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/incomingLot/getMtnBalesForGRN")
    fun postOfflineMtnWithBales(
        @Query("key") key: String,
        @Body mtn: PortMtn
    ): Call<GenericReqAndResp<GenericMessage>>

    @POST("x-ginning/port/incomingLot/getMtnInClassification")
    suspend fun getMtnInClassification(
        @Query("key") key: String,
        @Query("mtnId") mtnId: String
    ): GenericReqAndResp<GenericMessage>
}
