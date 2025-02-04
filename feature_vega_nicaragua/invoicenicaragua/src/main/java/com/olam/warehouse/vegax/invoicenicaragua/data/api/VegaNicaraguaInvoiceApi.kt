package com.olam.warehouse.vegax.invoicenicaragua.data.api

import com.olam.warehouse.master.common.modal.InventoryResponse
import com.olam.warehouse.master.common.model.AdvanceLineItems
import com.olam.warehouse.master.common.model.ExchangeRate
import com.olam.warehouse.master.common.model.VendorGrnDetailsResponse
import com.olam.warehouse.master.veganicaragua.model.VegaNicInvoiceReceipt
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaInvoicePostRequest
import com.olam.warehouse.vegax.invoicenicaragua.data.domain.model.VegaNicaraguaUpdateInvoiceSequencePost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 9/01/2020.
 */

interface VegaNicaraguaInvoiceApi
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

    @GET("x-master/getAdvanceLineItemDetails")
    suspend fun getAdvanceLineItems(
        @Query("key") key: String,
        @Query("vendorCode") vendorCode: String
    ): GenericReqAndResp<List<AdvanceLineItems>>

    @GET("x-master/getInventoryByLotAndMaterial")
    suspend fun getGrnInventoryDetails(

        @Query("key") key: String,
        @Query("lotId") lotId: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<InventoryResponse>

    @POST("x-pre-processing/vega/grn/saveInvoice")
    suspend fun postInvoiceData(
        @Body receivingData: VegaNicaraguaInvoicePostRequest
    ): GenericReqAndResp<VegaNicaraguaInvoicePostRequest>


    @POST("x-pre-processing/vega/grn/saveInvoice")
    fun syncInvoiceData(@Body receivingData: VegaNicaraguaInvoicePostRequest): Call<GenericReqAndResp<VegaNicaraguaInvoicePostRequest>>

    @GET("x-master/getInvoiceDetails")
    suspend fun getInvoiceReceipt(
        @Query("isReconcilation") isReconcilation: Boolean,
        @Query("key") key: String
    ): GenericReqAndResp<List<VegaNicInvoiceReceipt>>

    @POST("/x-master/lotSequence")
    fun updateInvoiceSequenceWorker(@Body postData: VegaNicaraguaUpdateInvoiceSequencePost): Call<GenericReqAndResp<VegaNicaraguaUpdateInvoiceSequencePost>>
}
