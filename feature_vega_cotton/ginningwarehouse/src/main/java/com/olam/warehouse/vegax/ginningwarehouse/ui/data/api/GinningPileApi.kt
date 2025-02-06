package com.olam.warehouse.vegax.ginningwarehouse.ui.data.api

import com.google.gson.JsonObject
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileSuccessResponse
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GinningPileApi {

    @GET("x-ginning/gininguser/getstoragelocationlist")
    suspend fun getStorageLocationList(@Query("key") key: String): GenericReqAndResp<List<GinningPileStorageLocationModel>>

    @POST("x-ginning/gininguser/getBaledetilsValid")
    suspend fun validateBale(
        @Query("key") key: String,
        @Body json: JsonObject
    ): GenericReqAndResp<List<PileBale>>

    @POST("x-ginning/gininguser/getBalePileDetails")
    suspend fun updateBaleToPile(
        @Query("key") key: String,
        @Body request: GinningPileStorageLocationModel
    ): GenericReqAndResp<GinningPileSuccessResponse>
}
