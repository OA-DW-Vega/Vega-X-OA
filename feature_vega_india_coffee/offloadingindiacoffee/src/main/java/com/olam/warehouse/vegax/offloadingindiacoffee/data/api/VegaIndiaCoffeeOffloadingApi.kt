package com.olam.warehouse.vegax.offloadingindiacoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeBatchNumResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.IndiaCoffeeOffloadingQualityPostResponse
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingLotQuality
import com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model.VegaIndiaCoffeeOffloadingQualityPost
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaIndiaCoffeeOffloadingApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaIndiaCoffeeOffloadingQualityPost): GenericReqAndResp<IndiaCoffeeOffloadingQualityPostResponse>

    @GET("x-master/getBatchNumberForMTNDelivery")
    suspend fun getDeliveryBatchNumber(
        @Query("key") key: String,
        @Query("deliveryOrderID") deliveryNo: String,
        @Query("deliveryItem") posnr: String
    ): GenericReqAndResp<IndiaCoffeeBatchNumResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeOffloadingLotQuality>>


    @POST("x-master/inventory/storageLocRecommendation")
    suspend fun getSuggestedLocation(
        @Query("kor") kor: String,
        @Query("origin") origin: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCustomStLocation>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
            @Query("key") currentKey: String,
            @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>
}
