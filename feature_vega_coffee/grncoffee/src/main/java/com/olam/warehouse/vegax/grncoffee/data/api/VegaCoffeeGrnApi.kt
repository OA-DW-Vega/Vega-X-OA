package com.olam.warehouse.vegax.grncoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnPost
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeGrnResponse
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoffeeGrnApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaGrnWeighBridgeId>>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaCoffeeGrnPost): GenericReqAndResp<VegaCoffeeGrnResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>
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
