package com.olam.warehouse.vegax.forwardponicaragua.data.api

import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnCharDetails
import com.olam.warehouse.master.common.model.GrnPriceDetails
import com.olam.warehouse.master.common.model.VendorGrnDetailsResponse
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPost
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaForwardPoPostResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.forwardponicaragua.data.domain.model.VegaNicaraguaUpdateLotSequencePost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */

interface VegaNicaraguaForwardPOApi
{

    @GET("x-master/getApprovalWBListByVendor")
    suspend fun getGrnDetails(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<VendorGrnDetailsResponse>>

    @GET("x-master/getExchangeRate")
    suspend fun getExchangeRate(
        @Query("appDate") date: String,
        @Query("key") key: String
    ): GenericReqAndResp<ExchangeRate>

    @GET("x-master/getGRNCharDetails")
    suspend fun getGrnCharDetails(
        @Query("grade") grade: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<GrnCharDetails>>

    @GET("x-master/getGRNPriceDetails")
    suspend fun getGrnPriceDetails(
        @Query("key") key: String
    ): GenericReqAndResp<List<GrnPriceDetails>>

    @POST("x-pre-processing/vega/grn/forward-po")
    suspend fun postForwardPo(
        @Body receivingData: VegaNicaraguaForwardPoPost
    ): GenericReqAndResp<VegaNicaraguaForwardPoPost>

    @POST("x-pre-processing/vega/grn/forward-po")
    fun syncForwardPOData(@Body receivingData: VegaNicaraguaForwardPoPost): Call<GenericReqAndResp<VegaNicaraguaForwardPoPost>>

    @POST("/x-master/lotSequence")
    suspend fun updateLotSequence(@Body postData: VegaNicaraguaUpdateLotSequencePost): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>

    @POST("/x-master/lotSequence")
    fun updateLotSequenceWorker(@Body postData: VegaNicaraguaUpdateLotSequencePost): Call<GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>>
}
