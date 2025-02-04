package com.olam.warehouse.vegax.portwarehouse.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.InventoryBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PilesMaster
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface InventoryApi {
    @GET("x-ginning/port/inventory/inventoryBales")
    suspend   fun fetchInventoryBaleList(
        @Query("key") key: String,
        @Query("baleNo") baleNo: String,
        @Query("baleType") baleTypeList: ArrayList<String>,
        @Query("grade") gradeList: ArrayList<String>,
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int,
        @Query("mark") marks: ArrayList<String>,
        @Query("crop") years: ArrayList<String>
    ): GenericReqAndResp<InventoryBale>

    @PUT("x-ginning/port/inventory/inventoryBales")
    suspend fun chageBaleStatus(
        @Query("key") key: String,
        @Body changeBaleStatus: ChangeBaleStatus
    ): GenericReqAndResp<GenericMessage>

    @GET("x-ginning/port/inventory/getGradeList")
    suspend fun getInventoryGrades(
        @Query("key") key: String,
        @Query("pile") selectedPileList: List<String>
    ): GenericReqAndResp<List<BaleGrade>>

    @GET("x-ginning/warehouse/pwBaleSyncByGrade")
    suspend fun syncBaleByGrade(
        @Query("key") key: String,
        @Query("grade") grade: String
    ): GenericReqAndResp<GenericMessage>

    @GET("x-ginning/port/pileManagement/getPileLists")
    suspend fun getPileList(@Query("key") key: String): GenericReqAndResp<PilesMaster>
}
