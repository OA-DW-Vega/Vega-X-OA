package com.olam.warehouse.vegax.grnecuador.data.api

import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Roshna Parambil on 1/13/2022.
 */

interface VegaEcuadorGrnApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaGrnWeighBridgeId>>
 @GET("x-master/getWBListDetails")
    suspend fun getWeighBridgeGrnPendingList(@Query("key") key: String, @Query("grnWbIdFlag") grnWbIdFlag: Boolean): GenericReqAndResp<List<VegaGrnWeighBridgeId>>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaEcuadorGrnPost): GenericReqAndResp<VegaEcuadorGrnResponse>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

}
