package com.olam.warehouse.odquality.data.api

import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.data.domain.model.DOQualityPost
import com.olam.warehouse.odquality.data.domain.model.DOQualityPostResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualitySavedResponse
import com.olam.warehouse.odquality.data.domain.model.DOQualityWeighBridgeBagDetail
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
interface DOQualityApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") currentKey: String): GenericReqAndResp<List<DOQualityWBDetails>>

    @GET("x-master/qa/lot-quality-details")
    suspend fun fetchSavedWeighBridgeDetail(@Query("plantId") plantId: String,
                                            @Query("materialNumber") materialNumber: String,
                                            @Query("batchNumber") batchNumber: String,
                                            @Query("key") key: String)
            : GenericReqAndResp<DOQualitySavedResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: DOQualityPost): GenericReqAndResp<DOQualityPostResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: DOQualityPost): Call<GenericReqAndResp<DOQualityPostResponse>>

    @GET("x-master/blt/fetchDoWeighBridgeBagDetails")
    suspend fun getDOWeighBridgeBagDetails(@Query("key") key: String, @Query("plantId") plantId: String)
            : GenericReqAndResp<DOQualityWeighBridgeBagDetail>
}
