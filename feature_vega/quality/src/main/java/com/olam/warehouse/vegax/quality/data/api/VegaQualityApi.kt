package com.olam.warehouse.vegax.quality.data.api

import com.olam.warehouse.master.vega.entity.VegaPostProcessQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPost
import com.olam.warehouse.vegax.quality.data.domain.model.VegaQualityPostResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaQualityApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaQualityPost): GenericReqAndResp<VegaQualityPostResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: VegaQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>


    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPostProcessQualityDetails(@Query("key") key: String): GenericReqAndResp<List<VegaPostProcessQualityWBDetails>>

}
