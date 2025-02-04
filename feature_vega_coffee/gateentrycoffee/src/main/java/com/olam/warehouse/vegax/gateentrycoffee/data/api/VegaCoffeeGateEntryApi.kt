package com.olam.warehouse.vegax.gateentrycoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.model.VegaGateEntryPost
import com.olam.warehouse.vegax.gateentrycoffee.data.domain.model.VegaGateEntryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoffeeGateEntryApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaGateEntry>>

    @GET("x-master/getWSListNoWeightDetails")
    suspend fun fetchWSWaitingTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryPost: VegaGateEntryPost): GenericReqAndResp<VegaGateEntryResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @POST("x-pre-processing/vega/wb/create-mtnr-wsid")
    suspend fun postWeighScaleGateEntryData(@Body gateEntryPost: VegaGateEntryPost): GenericReqAndResp<VegaGateEntryResponse>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>


    @GET("x-master/getWSIdDetails")
    suspend fun getWeighScaleIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>
}
