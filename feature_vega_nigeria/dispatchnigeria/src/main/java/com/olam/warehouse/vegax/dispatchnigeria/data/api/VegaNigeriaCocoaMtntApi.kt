package com.olam.warehouse.vegax.dispatchnigeria.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaNigeriaCocoaMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
//    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    suspend fun getPurchaseOrder(
        @Query("key") key: String
    ): GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaQualityApproveNigeria>>

    @POST("x-dispatch/vega/dispatch/create-delivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaNigeriaCocoaMtntDeliveryPost): GenericReqAndResp<VegaDeliveryPostResponse>

//    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
//    suspend fun postWeighScaleDeliveryDetails(@Body vegaDeliveryPost: VegaNigeriaSesameMtntDeliveryPost): GenericReqAndResp<List<VegaNigeriaSesameMtntDeliveryDetail>>

    @POST("x-dispatch/vega/dispatch/auto-delivery")
    suspend fun postWeighScaleDeliveryDetailsAuto(@Body vegaDeliveryPost: VegaNigeriaCocoaMtntMergedDeliveryPost): GenericReqAndResp<VegaNigeriaCocoaMergedDeliveryPostResponse>

    @POST("x-dispatch/vega/dispatch/binmerge")
    suspend fun postWeighScaleDeliveryDetailsBinMerge(@Body vegaDeliveryPost: VegaNigeriaCocoaMtntBinMerge): GenericReqAndResp<MergedData>

    @POST("x-dispatch/vega/dispatch/weightedAvg")
    suspend fun postWeightedAverage(@Body vegaWeightedAvgPost: VegaNigeriaCocoaWeightedAveragePost): GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>

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
    ): GenericReqAndResp<List<VegaNigeriaCocoaMtntWeighScalePallet>>

    @GET("x-master/getMTNDeliveryDetails")
    suspend fun getDelivery(
        @Query("key") key: String,
        @Query("deliveryOrderID") delivery: String,
        @Query("deliveryItem") deliveryItem: String
    ): GenericReqAndResp<VegaDispatchDelivery>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/qc/calculate-batch-characteristics")
    suspend fun postQualityNigeriaPost(@Body vegaQualityNigeriaPost: VegaQualityNigeriaPost): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>
}
