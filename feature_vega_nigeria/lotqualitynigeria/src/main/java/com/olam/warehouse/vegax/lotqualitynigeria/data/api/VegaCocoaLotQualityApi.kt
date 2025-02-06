package com.olam.warehouse.vegax.lotqualitynigeria.data.api

import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.lotqualitynigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*


interface VegaCocoaLotQualityApi {


    @GET("x-processing/quality/getInspectionLots")
    suspend fun getInspectionLots(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaCocoaLotQualityInspectionLots>>

    @GET("x-processing/quality/getInspectionLotDetails")
    suspend fun getInspectionLotDetails(
        @Query("key") key: String,
        @Query("inspectionLotId") lotId: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<VegaCocoaLotQualityInspectionLotDetails>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun saveInspectionLotDetails(
        @Body lotDetail: VegaCocoaQcPost
    ): GenericReqAndResp<VegaCocoaQualityApprovePostResponse>

    @POST("x-processing/quality/saveInspectionLotDetails")
    suspend fun saveInspectionLotDetails(
        @Query("key") key: String,
        @Query("plantId") plant: String,
        @Body lotDetail: VegaCocoaLotQualityInspectionLotDetails
    ): GenericReqAndResp<VegaCocoaLotQualityPostResponse>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaCocoaQcPost): GenericReqAndResp<VegaCocoaQualityApprovePostResponse>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaQualityPostLot): GenericReqAndResp<VegaQualityPostResponse>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getSesameInventoryList(@Query("key") key: String): GenericReqAndResp<VegaLotQualityModel>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-pre-processing/vega/qc/calculate-batch-characteristics")
    suspend fun postQualityNigeriaPost(@Body vegaQualityNigeriaPost: VegaQualityNigeriaLotPost): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>
}

