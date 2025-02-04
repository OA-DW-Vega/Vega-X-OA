package com.olam.warehouse.vegax.dispatchecuador.data.api

import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDispatchPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

/**
 * Created by Keerthi Santhanam on 7/12/2020.
 */
interface VegaEcuadorDispatchApi {

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByPlant")
    suspend fun getPurchaseOrder(
        @Query("key") key: String,
        @Query("receivingWerks") plantId: String
    ): GenericReqAndResp<List<VegaEcuadorDispatchPurchaseOrder>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLot(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: ArrayList<String>,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaEcuadorDispatchStocks>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaEcuadorDispatchStocks>>

    @POST("x-dispatch/vega/dispatch/auto-delivery")
    suspend fun postDeliveryDetail(@Body vegaDeliveryPost: VegaEcuadorDeliveryPost): GenericReqAndResp<VegaEcuadorDeliveryPostResponse>
}
