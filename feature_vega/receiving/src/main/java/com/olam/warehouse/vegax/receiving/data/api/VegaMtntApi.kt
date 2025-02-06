package com.olam.warehouse.vegax.receiving.data.api

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.receiving.data.domain.model.VegaMtntPost
import com.olam.warehouse.vegax.receiving.data.domain.model.VegaMtntResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
interface VegaMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun getWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaMtnt>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    suspend fun postMtntData(@Body mtntData: VegaMtntPost): GenericReqAndResp<VegaMtntResponse>

    //    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrder(@Query("key") key: String/*,  @Query("receivingWerks") receivingWerks: String*/): GenericReqAndResp<List<VegaPurchaseOrder>>
}
