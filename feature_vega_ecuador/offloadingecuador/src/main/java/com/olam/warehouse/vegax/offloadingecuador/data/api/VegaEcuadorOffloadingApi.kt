package com.olam.warehouse.vegax.offloadingecuador.data.api

import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 23/6/2020.
 */
interface VegaEcuadorOffloadingApi {

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postEcuadorOffloadingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>
}
