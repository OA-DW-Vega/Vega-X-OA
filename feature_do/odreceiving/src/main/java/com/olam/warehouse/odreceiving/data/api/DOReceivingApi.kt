package com.olam.warehouse.odreceiving.data.api

import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOSapMaterialList
import com.olam.warehouse.master.dorigin.entity.DOTransactionDetail
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.data.domain.model.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
interface DOReceivingApi {
    @POST("x-pre-processing/DO/wb/create-wbid")
    suspend fun postReceivingDetail(@Body receivingData: DOReceivingPost): GenericReqAndResp<DOReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: DOReceivingPost): GenericReqAndResp<DOReceivingResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<DOReceiving>): Call<GenericReqAndResp<String>>

    @POST("x-pre-processing/DO/wb/create-wbid")
    fun postReceivingItem(@Body receivingData: DOReceivingPost): Call<GenericReqAndResp<DOReceivingResponse>>

    /*@GET("master/mtn-details")
    suspend fun fetchWarehouseWithMtns(): GenericReqAndResp<DOReceivingMtnWrapper>*/

    @GET("x-master/getDOTransDetails")
    suspend fun getTransactionDetail(@Query("transId") id: String, @Query("key") currentKey: String): GenericReqAndResp<DOTxnDetail>

    @GET("x-master/getDOTransDetails")
    suspend fun getTransactionDetailBag(@Query("bagQrCode") qrCode: String, @Query("key") currentKey: String): GenericReqAndResp<DOTxnDetail>

    @GET("x-master/getDOTransList")
    suspend fun fetchTransactionList(@Query("key") key: String): GenericReqAndResp<List<DOTransactionDetail>>

    @POST("x-master/blt/fetchDispatchDetails/{key}")
    suspend fun getDispatchDetails(@Path("key") key: String, @Body dispatchDetailPost: DODispatchDetailPost): GenericReqAndResp<DispatchDetailsResponse>

    @GET("x-master/getSapMaterialByProductCode")
    suspend fun getSapMaterialByProductCode(@Query("key") currentKey: String):GenericReqAndResp<List<DOSapMaterialList>>
}
