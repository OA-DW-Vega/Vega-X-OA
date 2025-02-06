package com.olam.warehouse.vegax.pilesesame.data.api

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.pilesesame.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaSesamePileManagementApi {

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getPilesByMaterials")
    suspend fun getStockPile(
        @Query("key") currentKey: String,
        @Query("materialCode") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaSesamePileSelectionModel>>

    @POST("x-master/lotSequence/getLatestPileSequence")
    suspend fun getCreatePile(
        @Query("key") currentKey: String,
        @Body plant: VegaSesamePilePlantDetails
    ): GenericReqAndResp<VegaSesamePileSequence>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-dispatch/vega/dispatch/pilemgmt")
    suspend fun getPostPile(@Body postPileRequest: VegaSesamePilePostRequest): GenericReqAndResp<VegaSesamePileSuccessResponse>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaDispatchLotQuality>>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getLotQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaDispatchLotQuality>>


    @POST("/x-master/lotSequence")
    suspend fun updatePileSequence(@Body postData: NicaraguaUpdatePileSequence): GenericReqAndResp<NicaraguaUpdatePileSequence>

    @POST("x-dispatch/vega/dispatch/weightedAvg")
    suspend fun postWeightedAverage(@Body vegaWeightedAvgPost: VegaNigeriaSesameWeightedAveragePost): GenericReqAndResp<VegaNigeriaSesameWeightedAverageResponse>

}

