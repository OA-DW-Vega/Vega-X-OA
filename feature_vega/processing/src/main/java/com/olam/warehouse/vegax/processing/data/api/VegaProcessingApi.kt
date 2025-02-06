package com.olam.warehouse.vegax.processing.data.api

import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.processing.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaProcessingApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @POST("x-processing/vega/processing/fetchBom")
    suspend fun fetchBomList(@Body bomPostReq: VegaProcessingRminBomPost): GenericReqAndResp<VegaProcessingRminBom>


    @POST("x-processing/vega/processing/createProcessOrder")
    suspend fun postCreatePo(@Body bomPostReq: VegaProcessingCreatePoReq): GenericReqAndResp<VegaProcessingRminPo>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaProcessingQualityDetails>>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaProcessingQualityDetails>>>

    @GET("x-master/getStock")
    suspend fun getStocks(@Query("key") key: String, @Query("materialCode") material: String): GenericReqAndResp<List<VegaDispatchLots>>

    @POST("x-processing/vega/processing/processOrders")
    suspend fun getFgrnPoDetailsList(@Body vegaProcessingOrderReq: VegaProcessingOrderReq): GenericReqAndResp<List<VegaFgrnProcessingOrder>>

    @POST("x-processing/vega/processing/processOrdersDetails")
    suspend fun getFgrnGrades(@Body poReq: VegaProcessOrderDetailsPostReq): GenericReqAndResp<List<VegaFgrnGrades>>

    @POST("x-processing/vega/processing/rmin_fgrn")
    suspend fun postFgrnDetails(@Body bomPostReq: VegaProcessingFgrnPost): GenericReqAndResp<List<VegaProcessingFgrnResponse>>

}
