package com.olam.warehouse.vegax.inventorycoffee.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeInventoryStocks
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeeInventoryAndSyncModel
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoffeeInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeInventoryStocks>>
}
