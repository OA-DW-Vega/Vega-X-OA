package com.olam.warehouse.vegax.mtntcocoa.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCocoaMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByPlant")
    suspend fun getPurchaseOrder(
        @Query("key") key: String,
        @Query("receivingWerks") receivingWerks: String
    ): GenericReqAndResp<List<VegaCocoaPurchaseOrder>>

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrderForNoWeighment(
        @Query("key") key: String
    ): GenericReqAndResp<List<VegaCocoaPurchaseOrder>>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/isValidLotID")
    suspend fun getNoWeighmentQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>

    @POST("x-dispatch/vega/dispatch/create-delivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCocoaDeliveryPost): GenericReqAndResp<VegaDeliveryPostResponse>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getStockByMaterials")
    suspend fun getNoWeighmentStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCocoaNoWeighmentPallet>>

    @POST("x-dispatch/vega/dispatch/create-delivery/virtualplant")
    suspend fun postVirtualDeliveryDetails(@Body vegaDeliveryPost: VegaCocoaVirtualPostRequest): GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetailsWeighscale(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCocoaWeighScalePallet>>

    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
    suspend fun postWeighScaleDeliveryDetails(@Body vegaDeliveryPost: VegaCocoaWeighscaleDeliveryPost): GenericReqAndResp<List<VegaCocoaMtntDeliveryDetail>>


}
