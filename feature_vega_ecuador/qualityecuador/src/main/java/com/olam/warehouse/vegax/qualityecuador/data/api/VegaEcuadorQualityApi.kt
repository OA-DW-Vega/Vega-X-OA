package com.olam.warehouse.vegax.qualityecuador.data.api

import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorQualitySavedResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
interface VegaEcuadorQualityApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaQualityPost): GenericReqAndResp<VegaQualityPostResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: VegaQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>

    @GET("x-master/qa/lot-quality-details")
    suspend fun fetchSavedWeighBridgeDetail(
        @Query("plantId") plantId: String,
        @Query("materialNumber") materialNumber: String,
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String
    )
            : GenericReqAndResp<VegaEcuadorQualitySavedResponse>
}
