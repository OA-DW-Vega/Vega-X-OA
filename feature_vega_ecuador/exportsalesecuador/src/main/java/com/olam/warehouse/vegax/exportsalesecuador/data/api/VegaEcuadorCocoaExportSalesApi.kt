package com.olam.warehouse.vegax.exportsalesecuador.data.api

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.model.VegaCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesecuador.data.domain.model.VegaCoffeeExportSalesPostRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
interface VegaEcuadorCocoaExportSalesApi {

    @GET("x-dispatch/vega/salesDispatch/getSalesOrderDetailsList")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaCoffeeExportSalesOrderModel>>

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

    @POST("x-dispatch/vega/salesDispatch/createExportSales")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCoffeeExportSalesPostRequest): GenericReqAndResp<VegaCoffeeExportSalesPostRequest>
}
