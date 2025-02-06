package com.olam.warehouse.vegax.portwarehouse.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnDispatchPostResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */

interface MtnDispatchApi {

    @GET("x-ginning/port/mtnDispatch/getDeliveryDetails")
    suspend fun fetchDeliveryDetails(@Query("key") key: String): GenericReqAndResp<List<MtnDispatchDelivery>>

    @GET("x-ginning/port/mtnDispatch/isBaleValid")
    suspend fun validateBale(
        @Query("key") key: String,
        @Query("baleID") baleID: String,
        @Query("deliveryNumber") deliveryNumber: String
    ): GenericReqAndResp<MtnBale>

    @POST("x-ginning/port/mtnDispatch/saveDeliveryProcess")
    suspend fun postDispatch(
        @Query("key") key: String,
        @Body deliveryDto: MtnDispatchDelivery
    ): GenericReqAndResp<MtnDispatchPostResponse>

    @POST("x-ginning/port/mtnDispatch/saveDeliveryProcess")
    fun postOfflineDispatch(
        @Query("warehouseId") warehouseId: Int,
        @Body deliveryDto: MtnDispatchDelivery
    ): Call<GenericReqAndResp<MtnDispatchPostResponse>>
}
