package com.olam.warehouse.vegax.localsalescameroon.data.api

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesOrderModel
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesPallet
import com.olam.warehouse.vegax.localsalescameroon.data.domain.model.VegaCameroonSalesPostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaCameroonSalesApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(
        @Query("key") key: String,
        @Query("isSales") isSales: Boolean
    ): GenericReqAndResp<List<VegaCocoaSalesWB>>

    @GET("x-dispatch/vega/salesDispatch/getSalesOrderDetailsList")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaCameroonSalesOrderModel>>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLot(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>

    @POST("x-dispatch/vega/salesDispatch/createAutoSalesDelivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCameroonSalesPostRequest): GenericReqAndResp<VegaCameroonSalesPostRequest>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCameroonSalesPallet>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>
}
