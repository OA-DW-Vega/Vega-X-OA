package com.olam.warehouse.vegax.thirdpartysalescoffee.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWeighScalePallet
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCocoaThirdPartyApi {

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
}
