package com.olam.warehouse.vegax.ginningwarehouse.ui.data.api

import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.BaleGrade
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.ChangeBaleStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface InventoryApi {

    @GET("x-ginning/warehouse/getBaleDetails")
    suspend  fun fetchInventoryBaleList(
        @Query("key") key: String
    ): GenericReqAndResp<List<Bale>>

    @PUT("inventoryBales")
    suspend fun changeBaleStatus(
        @Query("key") key: String,
        @Body changeBaleStatus: ChangeBaleStatus
    ): GenericReqAndResp<GenericMessage>

    @GET("x-ginning/warehouse/getBaleSummary")
    suspend fun getInventoryGrades(@Query("key") key: String):GenericReqAndResp<List<BaleGrade>>

    @GET("x-ginning/warehouse/baleDataSyncByGrade")
    suspend fun syncBaleByGrade(@Query("key") key: String, @Query("grade") grade: String): GenericReqAndResp<GenericMessage>
}
