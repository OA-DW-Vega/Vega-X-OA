package com.olam.warehouse.vegax.localsalesecuador.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrder
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesOrderModel
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPallet
import com.olam.warehouse.vegax.localsalesecuador.data.domain.model.VegaCoffeeSalesPostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaEcuadorCocoaSalesApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(
        @Query("key") key: String,
        @Query("isSales") isSales: Boolean
    ): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-dispatch/vega/salesDispatch/getSalesOrderDetailsList")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>

    @GET("x-dispatch/vega/salesDispatch/getSalesOrderDetailsList")
    suspend fun getWBPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaPurchaseOrder>>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>


    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByPlant")
    suspend fun getPurchaseOrder(
        @Query("key") key: String,
        @Query("receivingWerks") receivingWerks: String
    ): GenericReqAndResp<List<VegaCocoaPurchaseOrder>>

    @GET("x-master/isValidLotID")
    suspend fun getWBQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>


    @GET("x-master/getStockByMaterials")
    suspend fun getStockListByWB(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLot(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>

    @POST("x-dispatch/vega/salesDispatch/createAutoSalesDelivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeSalesPostRequest): GenericReqAndResp<VegaCoffeeSalesPostRequest>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCoffeeSalesPallet>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCoffeeSalesLots>>

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaDispatchWB>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>
}
