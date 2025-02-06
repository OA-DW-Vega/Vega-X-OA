package com.olam.warehouse.vegax.exportsalesnigeria.data.api

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.exportsalesnigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
interface VegaNigeriaExportSalesApi {

    @GET("x-dispatch/vega/salesDispatch/getSalesOrderDetailsList")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaNigeriaExportSalesOrderModel>>

    @GET("x-containerManagement/container/getContainerDetails")
    suspend fun getContainerInventory(
        @Query("key") key: String,
        @Query("status") status: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<VegaNigeriaInventoryModel>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaNigeriaExportSalesWeighScalePallet>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCoffeeExportSalesLots>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLot(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeExportSalesLots>>

//    @POST("x-containerManagement/container/updateContainerStatus")
//    suspend fun updateContainerStatus(
//        @Query("containerNum") containerNum: String,
//        @Query("key") key: String,
//        @Query("status") status: String,
//        @Query("plantId") werks: String
//    ): GenericReqAndResp<VegaNigeriaContainerStatusUpdate>

    @POST("x-containerManagement/container/updateContainerStatus")
    suspend fun updateContainerStatus(
        @Body statusPost: VegaNigeriaContainerUpdate
    ): GenericReqAndResp<VegaNigeriaContainerStatusUpdate>

    @POST("x-dispatch/vega/salesDispatch/createExportSales")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaNigeriaExportSalesPostRequest): GenericReqAndResp<VegaNigeriaExportSalesPostRequest>
}
