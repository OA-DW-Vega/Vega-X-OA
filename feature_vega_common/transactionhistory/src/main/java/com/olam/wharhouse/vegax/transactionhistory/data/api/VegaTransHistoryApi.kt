package com.olam.wharhouse.vegax.transactionhistory.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaHistoryTranxMtnr
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTranxResponse
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
interface VegaTransHistoryApi
{
    @GET("/x-pre-processing/vega/wb/getPreprocessingTxnDetails")
    suspend fun fetchHistoryTranxGrn(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGRNHistoryTranxResponse>

    @GET("/x-pre-processing/vega/wb/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnr(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaHistoryTranxMtnr>

    @GET("/x-dispatch/vega/dispatch/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnt(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaMtntHistoryTranxResponse>


    @GET("/x-processing/vega/processing/getProcessingTxnDetails")
    suspend fun fetchHistoryTranxFgrn(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String,
        @Query("txnType") txnType: String
    ) : GenericReqAndResp<VegaFGRNHistoryTranxResponse>


    @GET("/x-processing/vega/processing/getProcessingTxnDetails")
    suspend fun fetchHistoryTranxRmin(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String,
        @Query("txnType") txnType: String
    ) : GenericReqAndResp<VegaRMINHistoryTranxResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String,
        @Query("werks") plantId: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

}
