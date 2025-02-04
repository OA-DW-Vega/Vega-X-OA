package com.olam.warehouse.vegax.inventorynicaragua.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryStocks
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
interface VegaNicInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaNicInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaNicInventoryStocks>>
}
