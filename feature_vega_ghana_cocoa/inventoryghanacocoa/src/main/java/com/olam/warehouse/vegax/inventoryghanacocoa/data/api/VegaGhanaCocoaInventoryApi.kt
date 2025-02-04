package com.olam.warehouse.vegax.inventoryghanacocoa.data.api

import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaGhanaInventoryStocks
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
interface VegaGhanaCocoaInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getSesameInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaGhanaInventoryStocks>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(
        @Query("key") currentKey: String,
        @Query("isInventory") isInventory: Boolean
    ): GenericReqAndResp<VegaReceivingMtnWrapper>
}
