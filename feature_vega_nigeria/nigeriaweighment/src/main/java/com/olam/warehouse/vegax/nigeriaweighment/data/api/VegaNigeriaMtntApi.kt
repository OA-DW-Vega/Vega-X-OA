package com.olam.warehouse.vegax.nigeriaweighment.data.api

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.nigeriaweighment.data.domain.model.VegaNigeriaMtntPost
import com.olam.warehouse.vegax.nigeriaweighment.data.domain.model.VegaNigeriaMtntResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaNigeriaMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun getWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaMtnt>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    suspend fun postMtntData(@Body nigeriaMtntData: VegaNigeriaMtntPost): GenericReqAndResp<VegaNigeriaMtntResponse>

    //    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaPurchaseOrder>>

}
