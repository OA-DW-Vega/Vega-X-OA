package com.olam.warehouse.vegax.inventorycocoa.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLotHead
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLots
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */
interface VegaCocoaInventoryApi {
    @GET("x-master/isValidLotID")
    suspend fun getScanLotDetail(
        @Query("lotID") lotId: String,
        @Query("key") currentKey: String,
        @Query("matnr") matnr: String,
        @Query("werks") plantId: String): GenericReqAndResp<List<VegaCocoaInventoryLots>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchQualityDetails(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<MaterialQuality>>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaCocoaInventoryLotHead>

}
