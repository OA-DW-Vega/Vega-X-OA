package com.olam.warehouse.vegax.inventoryghana.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryStocks
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaInventoryAndSyncModel
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 7/6/2020.
 */
interface VegaGhanaInventoryApi {
    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getSesameInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaGhanaInventoryStocks>>
}
