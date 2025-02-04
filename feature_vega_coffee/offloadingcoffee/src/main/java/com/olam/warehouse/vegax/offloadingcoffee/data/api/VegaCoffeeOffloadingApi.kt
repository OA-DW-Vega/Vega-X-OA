package com.olam.warehouse.vegax.offloadingcoffee.data.api

import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloadingcoffee.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoffeeOffloadingApi {
    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postReceivingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<VegaReceiving>): Call<GenericReqAndResp<String>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    fun postReceivingItem(@Body receivingData: VegaCoffeeReceivingPostLineItem): Call<GenericReqAndResp<VegaReceivingResponse>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaCoffeeReceiving>>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaCoffeeReceiving>>

    @GET("x-master/getWSListNoWeightDetails")
    suspend fun fetchTruckInWeighScaleDetail(@Query("key") key: String): GenericReqAndResp<List<VegaCoffeeReceiving>>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getWSIdDetails")
    suspend fun getWeighScaleIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>

    @POST("x-pre-processing/vega/wb/create-wsid")
    suspend fun postOffloadingDetail(@Body vegaOffloadingPost: VegaCoffeeOffloadingPostRequest): GenericReqAndResp<VegaMtntResponse>

    @POST("x-pre-processing/vega/wb/create-supplier-wbid")
    suspend fun postOffloadingSupplierDetail(@Body vegaOffloadingPost: VegaCoffeeOffloadingSupplierPostRequest): GenericReqAndResp<VegaMtntResponse>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaDispatchLotQuality>>>
}
