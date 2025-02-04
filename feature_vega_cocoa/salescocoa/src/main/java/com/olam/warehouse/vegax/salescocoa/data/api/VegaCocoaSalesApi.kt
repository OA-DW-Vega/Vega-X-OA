package com.olam.warehouse.vegax.salescocoa.data.api

import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesDeliveryDetail
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesOrderModel
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPallet
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaCocoaSalesApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(
        @Query("key") key: String,
        @Query("isSales") isSales: Boolean
//        @Query("isThirdParty") isThirdParty: Boolean
    ): GenericReqAndResp<List<VegaCocoaSalesWB>>

    @GET("x-dispatch/vega/salesDispatch/getSalesOrder")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaSalesOrderModel>>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaSalesLots>>

    @POST("x-dispatch/vega/salesDispatch/createSalesDelivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCocoaSalesPostRequest): GenericReqAndResp<VegaDeliveryPostResponse>

    @POST("x-dispatch/vega/salesDispatch/createSalesDelivery/anticipated")
    suspend fun postAnticipatedDeliveryDetail(@Body vegaDeliveryPost: VegaCocoaSalesPostRequest): GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCocoaSalesPallet>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaSalesLots>>
}
