package com.olam.warehouse.vegax.offloading.data.api

import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloading.data.domain.model.BatchNumResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.OffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingLotQuality
import com.olam.warehouse.vegax.offloading.data.domain.model.VegaOffloadingQualityPost
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaOffloadingApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaOffloadingQualityPost): GenericReqAndResp<OffloadingQualityPostResponse>

    @GET("x-master/getBatchNumberForMTNDelivery")
    suspend fun getDeliveryBatchNumber(@Query("key") key: String, @Query("deliveryOrderID") deliveryNo: String, @Query("deliveryItem") posnr: String): GenericReqAndResp<BatchNumResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaOffloadingLotQuality>>


    @POST("x-master/inventory/storageLocRecommendation")
    suspend fun getSuggestedLocation(
        @Query("kor") kor: String,
        @Query("origin") origin: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCustomStLocation>>
}
