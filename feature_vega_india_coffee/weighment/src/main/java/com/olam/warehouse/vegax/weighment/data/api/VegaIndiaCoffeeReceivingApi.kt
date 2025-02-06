package com.olam.warehouse.vegax.weighment.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingPostLineItem
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeReceivingResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaIndiaCoffeeReceivingApi {

    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postReceivingDetail(@Body indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPost): GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<VegaReceiving>): Call<GenericReqAndResp<String>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    fun postReceivingItem(@Body indiaCoffeeReceivingData: VegaIndiaCoffeeReceivingPostLineItem): Call<GenericReqAndResp<VegaIndiaCoffeeReceivingResponse>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
            @Query("key") key: String,
            @Query("charg") batchNo: String,
            @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

}
