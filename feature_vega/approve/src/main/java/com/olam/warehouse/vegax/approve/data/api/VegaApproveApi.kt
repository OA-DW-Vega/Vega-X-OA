package com.olam.warehouse.vegax.approve.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.approve.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

interface VegaApproveApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaApproveWeighBridgeId>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaApproveQuality>>

    @POST("pre-processing-ca/approve/bc-approval")
    suspend fun approveWbid(
        @Body aapprovePostRequest: VegaApprovePostData
    ): GenericReqAndResp<VegaApproveWeighBridgeId>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaGrnPost): GenericReqAndResp<VegaGrnResponse>

    @POST("x-pre-processing/vega/approve/bc-approval ")
    suspend fun postApproval(@Body postApprovalData: PostApprovalData): GenericReqAndResp<VegaApprovalResponse>
}
