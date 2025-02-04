package com.olam.warehouse.vegax.mtntcameroon.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonDeliveryPost
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonMtntDeliveryDetail
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonPurchaseOrder
import com.olam.warehouse.vegax.mtntcameroon.data.domain.model.VegaCameroonWeighScalePallet
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCameroonMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
//    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    suspend fun getPurchaseOrder(
        @Query("key") key: String
    ): GenericReqAndResp<List<VegaCameroonPurchaseOrder>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-dispatch/vega/dispatch/create-delivery-mtnt")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCameroonDeliveryPost): GenericReqAndResp<VegaDeliveryPostResponse>

    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
    suspend fun postWeighScaleDeliveryDetails(@Body vegaDeliveryPost: VegaCameroonDeliveryPost): GenericReqAndResp<List<VegaCameroonMtntDeliveryDetail>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCameroonWeighScalePallet>>

    @GET("x-master/getMTNDeliveryDetails")
    suspend fun getDelivery(
        @Query("key") key: String,
        @Query("deliveryOrderID") delivery: String,
        @Query("deliveryItem") deliveryItem: String
    ): GenericReqAndResp<VegaDispatchDelivery>
}
