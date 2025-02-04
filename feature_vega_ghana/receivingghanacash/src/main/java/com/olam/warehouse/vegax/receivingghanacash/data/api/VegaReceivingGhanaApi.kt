package com.olam.warehouse.vegax.receivingghanacash.data.api

import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaPost
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaPostLineItem
import com.olam.warehouse.vegax.receivingghanacash.data.domain.model.VegaReceivingGhanaResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
interface VegaReceivingGhanaApi {

    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postReceivingDetail(@Body receivingData: VegaReceivingGhanaPost): GenericReqAndResp<VegaReceivingGhanaResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: VegaReceivingGhanaPost): GenericReqAndResp<VegaReceivingGhanaResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<VegaReceiving>): Call<GenericReqAndResp<String>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    fun postReceivingItem(@Body receivingData: VegaReceivingGhanaPostLineItem): Call<GenericReqAndResp<VegaReceivingGhanaResponse>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(@Query("key") key: String, @Query("wbId") wbId: String): GenericReqAndResp<VegaReceiving>

}
