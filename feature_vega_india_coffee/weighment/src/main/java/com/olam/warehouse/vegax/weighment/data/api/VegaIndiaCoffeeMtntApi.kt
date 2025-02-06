package com.olam.warehouse.vegax.weighment.data.api

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntPost
import com.olam.warehouse.vegax.weighment.data.domain.model.VegaIndiaCoffeeMtntResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaIndiaCoffeeMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun getWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaMtnt>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    suspend fun postMtntData(@Body indiaCoffeeMtntData: VegaIndiaCoffeeMtntPost): GenericReqAndResp<VegaIndiaCoffeeMtntResponse>

    //    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrder(@Query("key") key: String/*,  @Query("receivingWerks") receivingWerks: String*/): GenericReqAndResp<List<VegaPurchaseOrder>>
}
