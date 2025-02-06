package com.olam.warehouse.vegax.inventorynigeria.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaCocoaInventoryStocks
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaNigeriaInventoryLots
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
interface VegaNigeriaInventoryApi {
    @GET("x-master/isValidLotID")
    suspend fun getScanLotDetail(
        @Query("lotID") lotId: String,
        @Query("key") currentKey: String,
        @Query("matnr") matnr: String,
        @Query("werks") plantId: String
    ): GenericReqAndResp<List<VegaNigeriaInventoryLots>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchQualityDetails(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<MaterialQuality>>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaInventoryStocks>>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getInventoryList(@Query("key") key: String): GenericReqAndResp<VegaInventoryAndSyncModel>

}
