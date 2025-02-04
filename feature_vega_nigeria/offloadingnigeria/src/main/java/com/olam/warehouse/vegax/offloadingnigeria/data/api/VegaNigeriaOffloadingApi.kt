package com.olam.warehouse.vegax.offloadingnigeria.data.api

import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaCocoaWeighScalePallet
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.offloadingnigeria.data.domain.usecase.model.VegaNigeriaOffloadingPostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
interface VegaNigeriaOffloadingApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postEcuadorOffloadingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>

    @POST("x-pre-processing/vega/wb/create-wsid")
    suspend fun postOffloadingDetail(@Body vegaOffloadingPost: VegaNigeriaOffloadingPostRequest): GenericReqAndResp<VegaMtntResponse>

    @GET("x-master/getWBListDetailsforQuality")
    suspend fun fetchQCWeighBridgeList(
        @Query("key") key: String,
        @Query("qcFlag") qcFlag: String,
        @Query("plantId") selectedPlantId: String
    ): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetailsWeighscale(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCocoaWeighScalePallet>>

    @GET("x-master/getDMSUploadedImages")
    suspend fun getDMSUploadedImages(
        @Query("key") key: String,
        @Query("orgType") orgType: String,
        @Query("wbId") wbId: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<List<VegaDMSImageResponse>>
}
