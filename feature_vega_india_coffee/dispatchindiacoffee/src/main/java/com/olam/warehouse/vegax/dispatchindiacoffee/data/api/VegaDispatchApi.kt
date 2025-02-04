package com.olam.warehouse.vegax.dispatchindiacoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDeliveryPost
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDeliveryPostResponse
import com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model.VegaDispatchLotQuality
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
interface VegaDispatchApi {
    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(
        @Query("key") key: String,
        @Query("isSales") isSales: Boolean
    ): GenericReqAndResp<List<VegaDispatchTrucks>>

    @GET("x-master/getStock")
    suspend fun getStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaDispatchLots>>

    @POST("x-dispatch/vega/dispatch/create-delivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaDeliveryPost): GenericReqAndResp<VegaDeliveryPostResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaDispatchLotQuality>>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaDispatchLotQuality>>>

    @GET("x-master/getMTNDeliveryDetails")
    suspend fun getDelivery(
        @Query("key") key: String,
        @Query("deliveryOrderID") delivery: String,
        @Query("deliveryItem") deliveryItem: String
    ): GenericReqAndResp<VegaDispatchDelivery>
}
