package com.olam.warehouse.ginning.data.api

import com.olam.warehouse.ginning.data.model.DispatchPostResponse
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 3/20/2020.
 */
interface GinningDispatchApi {

    @GET("x-ginning/warehouse/getDeliveryDetails")
    suspend  fun fetchDeliveryDetails(@Query("key") key: String): GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>
    @GET("x-ginning/warehouse/isBaleValid")
    suspend  fun validateBale(
        @Query("key") key: String,
        @Query("baleID") baleID: String,
        @Query("deliveryNumber") deliveryNumber: String
    ):  GenericReqAndResp<Bale>

    @POST("x-ginning/warehouse/saveDeliveryProcess")
    suspend   fun postDispatch(
        @Query("key") key: String,
        @Body deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery
    ): GenericReqAndResp<DispatchPostResponse>

    @POST("x-ginning/warehouse/saveDeliveryProcess")
    fun postOfflineDispatch(
        @Query("key") key: String,
        @Body deliveryDtoVegaCotton: VegaCottonGinningDispatchDelivery
    ): Call<GenericReqAndResp<DispatchPostResponse>>
}
