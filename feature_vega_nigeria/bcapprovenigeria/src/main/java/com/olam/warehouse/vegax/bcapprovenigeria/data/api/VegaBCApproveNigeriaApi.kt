package com.olam.warehouse.vegax.bcapprovenigeria.data.api

import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.bcapprovenigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

interface VegaBCApproveNigeriaApi {

/*
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaQualityApproveNigeriaWeighBridgeId>>
*/

    @GET("x-master/getApprovalWBList")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaQualityApproveCameroonWeighBridge>>

    @GET("x-master/getWBIdDetails")
    suspend fun getMtntWeightDetails(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaNigeriaWeighmentDetails>

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun getWeightbridgeListDetails(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String
    ): GenericReqAndResp<List<VegaNigeriaWeighmentDetails>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaQualityApproveNigeria>>

    @POST("pre-processing-ca/approve/bc-approval")
    suspend fun approveWbid(
        @Body aapprovePostRequest: VegaQualityApproveCameroonPostData
    ): GenericReqAndResp<VegaQualityApproveNigeriaWeighBridgeId>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaQualityApproveNigeriaGrnPost): GenericReqAndResp<VegaBcApproveNigeriaGrnResponse>

    //    @POST("x-pre-processing/vega/approve/bc-approval ")
    @POST("x-pre-processing/vega/approve/ec/bc-approval ")
    suspend fun postApproval(@Body postApprovalData: PostApprovalData): GenericReqAndResp<VegaBcApproveNigeriaResponse>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaNigeriaQualityApprovePost): GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>
}
