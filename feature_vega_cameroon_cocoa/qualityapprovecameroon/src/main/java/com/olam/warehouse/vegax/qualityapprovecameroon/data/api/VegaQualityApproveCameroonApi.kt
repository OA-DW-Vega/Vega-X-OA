package com.olam.warehouse.vegax.qualityapprovecameroon.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

interface VegaQualityApproveCameroonApi {

    //    @GET("x-master/getWBListDetails")
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
    ): GenericReqAndResp<VegaCameroonWeighmentDetails>

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun getWeightbridgeListDetails(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String
    ): GenericReqAndResp<List<VegaCameroonWeighmentDetails>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaQualityApproveCameroon>>

    //Added Get call of DSE API Call
    @GET("x-pre-processing/vega/wb/getDSEResponseMessages")
    suspend fun getDSEResponse(
        @Query("key") key: String,
        @Query("status") status: String
    ): GenericReqAndResp<VegaQualityApproveDSE>


    @POST("pre-processing-ca/approve/bc-approval")
    suspend fun approveWbid(
        @Body aapprovePostRequest: VegaQualityApproveCameroonPostData
    ): GenericReqAndResp<VegaQualityApproveCameroonWeighBridgeId>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaQualityApproveCameroonGrnPost): GenericReqAndResp<VegaQualityApproveCameroonGrnResponse>

    //    @POST("x-pre-processing/vega/approve/bc-approval ")
    @POST("x-pre-processing/vega/approve/ec/bc-approval ")
    suspend fun postApproval(@Body postApprovalData: PostApprovalData): GenericReqAndResp<VegaQualityApproveCameroonResponse>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaCameroonQcPost): GenericReqAndResp<VegaCameroonQualityApprovePostResponse>

    @Multipart
    @POST("/x-dispatch/reprint/upload/file")
    suspend fun postprintformatdata(
        @Part("moduleName") moduleName : RequestBody,
        @Part("type") type : RequestBody,
        @Part("moduleNo") moduleNo : RequestBody,
        @Part("trasanctionNo") trasanctionNo : RequestBody,
        @Part("materialName") materialName : RequestBody,
        @Part("date") date : RequestBody,
        @Part("companyCode") companyCode : RequestBody,
        @Part("file") file : RequestBody
    ): Call<GenericReqAndResp<CameroonSavePrintTicket>>

    @GET("x-dispatch/reprint/list")
    suspend fun getReprintList(
        @Query("moduleName") moduleName: String,
        @Query("companyCode") companycode: String
    ):  GenericReqAndResp<List<VegaReprintList>>

    @GET("x-dispatch/reprint/download")
    suspend fun downloadReprintItems(
        @Query("id") itemId: String,
    ):  GenericReqAndResp<String>
}
