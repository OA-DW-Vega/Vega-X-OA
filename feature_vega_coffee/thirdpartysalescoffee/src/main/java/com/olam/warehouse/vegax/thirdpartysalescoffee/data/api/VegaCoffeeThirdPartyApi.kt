package com.olam.warehouse.vegax.thirdpartysalescoffee.data.api

import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoffeeThirdPartyApi {


    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(
        @Query("key") key: String,
        @Query("isThirdParty") isSales: Boolean
    ): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-dispatch/vega/thirdpartysales/ownership-transfer")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeTPDeliveryPost): GenericReqAndResp<VegaDeliveryPostResponse>

    @POST("x-dispatch/vega/thirdpartysales/thirdparty-transfer")
    suspend fun postSameTPDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeTPDeliveryPost): GenericReqAndResp<List<VegaDeliveryPostResponse>>

    @POST("x-dispatch/vega/thirdpartysales/thirdparty-olam-transfer")
    suspend fun postTPtoOlamDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeTPDeliveryPost): GenericReqAndResp<List<VegaDeliveryPostResponse>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCoffeeWeighScalePallet>>

    @POST("x-dispatch/vega/thirdpartysales/thirdparty-transfer")
    suspend fun getPostPile(@Body postPileRequest: VegaCoffeeWeighbridgePostRequest): GenericReqAndResp<VegaCoffeeWeighbridgePostRequest>

    @POST("x-dispatch/vega/salesDispatch/create-sales-wbid")
    suspend fun postWeighbridgeData(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @GET("x-master/getGRNPriceDetails")
    suspend fun getGrnPriceDetails(
        @Query("key") key: String
    ): GenericReqAndResp<List<GrnPriceDetails>>

    @GET("x-master/getExchangeRate")
    suspend fun getExchangeRate(
        @Query("appDate") date: String,
        @Query("key") key: String
    ): GenericReqAndResp<ExchangeRate>


    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-dispatch/vega/thirdpartysales/thirdparty-olam-transfer")
    suspend fun postNicTPtoOlamDeliveryDetail(@Body vegaDeliveryPost: VegaNicaraguaCoffeeTPDeliveryPost): GenericReqAndResp<List<VegaDeliveryPostResponse>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @POST("/x-master/lotSequence")
    fun updateLotSequenceWorker(@Body postData: UpdateTPLotSequencePost): Call<GenericReqAndResp<UpdateTPLotSequencePost>>

}
