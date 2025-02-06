package com.olam.warehouse.vegax.splitlot.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.splitlot.data.domain.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Baskaran Kannan on 9/26/2022.
 */
interface VegaCommonSplitLotApi {
    @GET("x-master/getLotQualityDetails")
    suspend fun getLotQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-pre-processing/vega/qc/split-batch")
    suspend fun postSplitDeatils(@Body postData: VegaCommonSplitMainModel): GenericReqAndResp<VegaCommonSplitMainModel>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    suspend fun postQuality(@Body paramPost: VegaCoffeeQualityParamPost): GenericReqAndResp<VegaCoffeeQualityParamPostResponse>

    @POST("/x-master/lotSequence")
    suspend fun updateLotSequence(@Body postData: SplitUpdateTallySequencePost): GenericReqAndResp<SplitUpdateTallySequencePost>

    @POST("/x-dispatch/reprint/upload/file")
    fun saveprintformatWorker(@Body postData: VegaSplitLotSavePrintTicket): Call<GenericReqAndResp<VegaSplitLotSavePrintTicket>>

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
        @Part ("file") file : RequestBody
    ): Call<GenericReqAndResp<VegaSplitLotSavePrintTicket>>

}
