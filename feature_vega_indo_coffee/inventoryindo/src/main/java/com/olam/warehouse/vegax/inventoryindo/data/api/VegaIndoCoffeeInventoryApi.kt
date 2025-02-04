package com.olam.warehouse.vegax.inventoryindo.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLotHead
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLots
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
interface VegaIndoCoffeeInventoryApi {
    @GET("x-master/isValidLotID")
    suspend fun getScanLotDetail(
        @Query("lotID") lotId: String,
        @Query("key") currentKey: String,
        @Query("matnr") matnr: String,
        @Query("werks") plantId: String
    ): GenericReqAndResp<List<VegaIndoCoffeeInventoryLots>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchQualityDetails(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<MaterialQuality>>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaIndoCoffeeInventoryLotHead>
}
