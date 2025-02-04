package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaQualityApproveIndiaCoffeeApi {

    //    @GET("x-master/getWBListDetails")

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>


    @GET("x-master/getWBListDetailsforQuality")
    suspend fun fetchQCWeighBridgeList(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String,
        @Query("plantId") selectedPlantId: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getApprovalWBList")
    suspend fun fetchWeighBridgeList(
        @Query("key") key: String,
        @Query("werks") selectedPlantId: String
    ): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>

    //    @GET("x-master/getWBIdDetails")
    @GET("x-master/getWSIdDetails")
    suspend fun getMtntWeightDetails(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun getWeightbridgeListDetails(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeWeighmentDetails>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>

    @POST("pre-processing-ca/approve/bc-approval")
    suspend fun approveWbid(
        @Body aapprovePostRequest: VegaQualityApproveCameroonPostData
    ): GenericReqAndResp<VegaQualityApproveIndiaCoffeeWeighBridgeId>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaQualityApproveIndiaCoffeeGrnPost): GenericReqAndResp<VegaQualityApproveIndiaCoffeeGrnResponse>

    //    @POST("x-pre-processing/vega/approve/bc-approval ")
    @POST("x-pre-processing/vega/approve/ec/bc-approval ")
    suspend fun postApproval(@Body postApprovalData: PostApprovalData): GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaCameroonQcPost): GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>
}
