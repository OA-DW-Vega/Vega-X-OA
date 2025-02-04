package com.olam.warehouse.vegax.thirdpartysalescoffee.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighbridgePostRequest
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

}
