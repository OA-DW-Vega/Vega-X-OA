package com.olam.warehouse.vegax.receivingghanacash.data.api

import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.model.VegaPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntPost
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaMtntResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
interface VegaReceivingGhanaMtntApi {

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun getWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaMtnt>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    suspend fun postMtntData(@Body mtntData: VegaReceivingGhanaMtntPost): GenericReqAndResp<VegaReceivingGhanaMtntResponse>

    @GET("x-dispatch/vega/dispatch/getPurchaseOrders")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaPurchaseOrder>>
}
