package com.olam.warehouse.vegax.advanceniicaragua.data.api

import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostRequest
import com.olam.warehouse.vegax.advanceniicaragua.data.domain.model.VegaNicaraguaAdvancePostResponse
import com.olam.warehouse.master.veganicaragua.model.VegaNicaraguaGetAdvanceDetails
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface VegaNicaraguaAdvanceApi
{
    @GET("x-master/getVedorDetailsforCreditLimit")
    suspend fun getAdvanceDetails(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<VegaNicaraguaGetAdvanceDetails>>

    @GET("x-master/getExchangeRate")
    suspend fun getExchangeRate(
        @Query("appDate") date: String,
        @Query("key") key: String
    ): GenericReqAndResp<ExchangeRate>

    @POST("x-pre-processing/vega/grn/getVendorWiseDetailsforadvance")
    suspend fun postAdvanceData(
        @Body receivingData: VegaNicaraguaAdvancePostRequest
    ): GenericReqAndResp<VegaNicaraguaAdvancePostResponse>

    @POST("x-pre-processing/vega/grn/getVendorWiseDetailsforadvance")
    fun syncAdvanceData(@Body receivingData: VegaNicaraguaAdvancePostRequest): Call<GenericReqAndResp<VegaNicaraguaAdvancePostResponse>>


}
