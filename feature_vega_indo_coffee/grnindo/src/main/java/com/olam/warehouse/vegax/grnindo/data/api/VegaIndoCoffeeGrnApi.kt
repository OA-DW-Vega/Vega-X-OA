package com.olam.warehouse.vegax.grnindo.data.api

import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaUploadPrintRequest
import com.olam.warehouse.master.vega.entity.VegaUploadPrintResponse
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 4/8/2021.
 */
interface VegaIndoCoffeeGrnApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaGrnWeighBridgeId>>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaEcuadorGrnPost): GenericReqAndResp<VegaEcuadorGrnResponse>

    @POST("x-pre-processing/vega/gr/create-grn")
    fun postGrnTrans(@Body grnPost: VegaEcuadorGrnPost): Call<GenericReqAndResp<VegaEcuadorGrnResponse>>

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>
    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @POST("x-pre-processing/vega/dms/upload")
    suspend fun uploadGrnPrint(
        @Body grnPrint: VegaUploadPrintRequest
    ): GenericReqAndResp<VegaUploadPrintResponse>

//    @GET("x-master/getWSIdDetails")
//    suspend fun getWeighScaleIdDetail(
//        @Query("key") key: String,
//        @Query("wbId") wbId: String
//    ): GenericReqAndResp<VegaReceiving>
}
