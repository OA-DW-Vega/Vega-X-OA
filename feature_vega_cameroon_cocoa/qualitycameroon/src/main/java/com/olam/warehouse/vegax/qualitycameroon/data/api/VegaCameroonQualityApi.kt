package com.olam.warehouse.vegax.qualitycameroon.data.api

import com.olam.warehouse.master.common.model.VegaQualityPost
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityParamPost
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityPost
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualityPostResponse
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualitySecretId
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
interface VegaCameroonQualityApi {

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String,
        @Query("qcFlag") qcFlag: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchMtnrWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaCameroonQualityPost): GenericReqAndResp<VegaCameroonQualityPostResponse>

    @POST("x-pre-processing/DO/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: VegaQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>

    //==== Start MTNR ===


    @GET("x-master/getWBIdDetailsForPlant")
    suspend fun fetchLotDetail(
        @Query("key") currentKey: String,
        @Query("wbId") weighBridgeId: String
    ): GenericReqAndResp<List<VegaCoffeeLot>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    suspend fun postQuality(@Body paramPost: VegaCameroonQualityParamPost): GenericReqAndResp<VegaCameroonQualityParamPost>

    //==== ENd MTNR =====

    @GET("x-master/getReprintDetailsForGRN")
    suspend fun getDashboardResult(
        @Query("key") key: String,
        @Query("isMTNT") isMTNT: Boolean,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): GenericReqAndResp<List<VegaCameroonQualitySecretId>>


}
