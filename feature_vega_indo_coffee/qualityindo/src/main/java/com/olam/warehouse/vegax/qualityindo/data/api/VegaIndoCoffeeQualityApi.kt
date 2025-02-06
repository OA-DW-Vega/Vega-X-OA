package com.olam.warehouse.vegax.qualityindo.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
interface VegaIndoCoffeeQualityApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getWBIdDetailsForPlant")
    suspend fun fetchLotDetail(
        @Query("key") currentKey: String,
        @Query("wbId") weighBridgeId: String
    ): GenericReqAndResp<List<VegaCoffeeLot>>

    @GET("x-master/getWBIdDetails")
    suspend fun fetchLotDetailForDual(
        @Query("key") currentKey: String,
        @Query("wbId") weighBridgeId: String
    ): GenericReqAndResp<VegaCoffeeLot>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    suspend fun postQuality(@Body paramPost: VegaIndoCoffeeQualityParamPost): GenericReqAndResp<VegaIndoCoffeeQualityParamPost>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    fun postQualityOffline(@Body paramPost: VegaIndoCoffeeQualityParamPost): Call<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQualitySupplier(@Body paramPost: VegaIndoCoffeeQualitySupplierParamPost): GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>

    @POST("x-pre-processing/vega/qc/save-quality")
    fun postQualitySupplierOffline(@Body paramPost: VegaIndoCoffeeQualitySupplierParamPost): Call<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>

    @GET("x-master/getWSIdDetails")
    suspend fun getWeighScaleIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>
}
