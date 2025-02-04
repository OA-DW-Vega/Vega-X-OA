package com.olam.warehouse.vegax.reconcilnicaragua.data.api

import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.GrnDetailsResponse
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItems
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.GET
import retrofit2.http.Query


interface VegaNicaraguaReconcilReportApi
{
    @GET("x-master/getApprovalWBListByVendor")
    suspend fun getGrnDetails(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<GrnDetailsResponse>>

    @GET("x-master/getExchangeRate")
    suspend fun getExchangeRate(
        @Query("appDate") date: String,
        @Query("key") key: String
    ): GenericReqAndResp<ExchangeRate>

    @GET("x-master/getAdvanceLineItemDetails")
    suspend fun getAdvanceLineItems(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<VegaNicaraguaAdvanceLineItems>>
}
