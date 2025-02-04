package com.olam.warehouse.vegax.grnnicaragua.data.api

import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGrnPost
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.grnnicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */

interface VegaNicaraguaGrnApi
{
    @GET("x-master/getExchangeRate")
    suspend fun getExchangeRate(
        @Query("appDate") date: String,
        @Query("key") key: String
    ): GenericReqAndResp<ExchangeRate>

    @POST("x-pre-processing/vega/grn/create-grn")
    suspend fun postGrnData(
        @Body receivingData: VegaNicaraguaGrnPost
    ): GenericReqAndResp<VegaNicaraguaGrnPost>

    @POST("x-pre-processing/vega/grn/create-grn")
    fun syncGrnData(@Body receivingData: VegaNicaraguaGrnPost): Call<GenericReqAndResp<VegaNicaraguaGrnPost>>

    @GET("x-master/getGRNPriceDetails")
    suspend fun getGrnPriceDetails(
        @Query("key") key: String
    ): GenericReqAndResp<List<GrnPriceDetails>>

    @GET("x-master/getGRNCharDetails")
    suspend fun getGrnCharDetails(
        @Query("grade") grade: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<GrnCharDetails>>

    @GET("x-master/getAdvanceLineItemDetails")
    suspend fun getAdvanceLineItems(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<AdvanceLineItems>>

    @POST("/x-master/lotSequence")
    suspend fun updateLotSequence(@Body postData: VegaNicaraguaUpdateLotSequencePost): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>

    @POST("/x-master/lotSequence")
    fun updateLotSequenceWorker(@Body postData: VegaNicaraguaUpdateLotSequencePost): Call<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @GET("x-master/getReprintDetailsForGRN")
    suspend fun getGrnPrintDetails(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>
}
