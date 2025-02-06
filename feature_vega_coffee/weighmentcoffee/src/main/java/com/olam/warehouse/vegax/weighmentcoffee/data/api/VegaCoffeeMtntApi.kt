package com.olam.warehouse.vegax.weighmentcoffee.data.api

import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.weighmentcoffee.data.domain.usecase.VegaCoffeeSalesPostRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 9/2/2020.
 */

interface VegaCoffeeMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun getWeighBridgeDetail(
        @Query("key") key: String,
        @Query("isSales") isSales: Boolean,
        @Query("isThirdParty") isThirdParty: Boolean
    ): GenericReqAndResp<List<VegaMtnt>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    suspend fun postMtntData(@Body mtntData: VegaMtntPost): GenericReqAndResp<VegaMtntResponse>

    @POST("x-dispatch/vega/salesDispatch/create-sales-wbid")
    suspend fun postSalesData(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    //    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaPurchaseOrder>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postReceivingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<VegaReceiving>): Call<GenericReqAndResp<String>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    //  @POST("x-dispatch/vega/thirdpartysales/thirdparty-transfer")
    @POST("x-dispatch/vega/salesDispatch/create-sales-truckout-pgi")
    suspend fun postSalesTruckOut(@Body vegaDeliveryPost: VegaCoffeeSalesPostRequest): GenericReqAndResp<VegaCoffeeSalesPostRequest>

    @POST("x-dispatch/vega/thirdpartysales/thirdpartyTruckout")
    suspend fun postThirdPartyTruckOut(@Body vegaDeliveryPost: VegaMtntPost): GenericReqAndResp<Data>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @POST("x-dispatch/vega/salesDispatch/create-sales-truckout-pgi")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeSalesPostRequest): GenericReqAndResp<VegaCoffeeSalesPostRequest>
}
