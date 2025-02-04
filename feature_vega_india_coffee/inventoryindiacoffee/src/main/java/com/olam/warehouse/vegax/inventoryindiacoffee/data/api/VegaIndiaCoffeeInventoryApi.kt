package com.olam.warehouse.vegax.inventoryindiacoffee.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaIndiaCoffeeInventoryStocks
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface VegaIndiaCoffeeInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getIndiaCoffeeInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeInventoryStocks>>
}
