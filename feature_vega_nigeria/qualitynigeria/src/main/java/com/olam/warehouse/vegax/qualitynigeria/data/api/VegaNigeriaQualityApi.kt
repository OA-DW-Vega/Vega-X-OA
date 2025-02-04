package com.olam.warehouse.vegax.qualitynigeria.data.api

import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
interface VegaNigeriaQualityApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaQualityPost): GenericReqAndResp<VegaQualityPostResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: VegaQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun fetchQCWeighBridgeList(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String,
        @Query("plantId") selectedPlantId: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @POST("x-pre-processing/vega/qc/calculate-batch-characteristics")
    suspend fun postQualityNigeriaPost(@Body vegaQualityNigeriaPost: VegaQualityNigeriaPost): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>
}
