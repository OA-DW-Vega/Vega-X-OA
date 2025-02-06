package com.olam.wharhouse.vegax.transhistoryghanacashew.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 10/8/2022.
 */
interface VegaGhanaCashewTransHisApi
{
    @GET("/x-pre-processing/vega/wb/getPreprocessingTxnDetails")
    suspend fun fetchHistoryTranxGrn(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCashewGRNHistoryTranxResponse>

    @GET("/x-pre-processing/vega/wb/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnr(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCashewHistoryTranxMtnr>

    @GET("/x-dispatch/vega/dispatch/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnt(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCashewMtntHistoryTranxResponse>


    @GET("/x-processing/vega/processing/getProcessingTxnDetails")
    suspend fun fetchHistoryTranxFgrn(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String,
        @Query("txnType") txnType: String
    ) : GenericReqAndResp<VegaGhanaCashewFGRNHistoryTranxResponse>


    @GET("/x-processing/vega/processing/getProcessingTxnDetails")
    suspend fun fetchHistoryTranxRmin(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String,
        @Query("txnType") txnType: String
    ) : GenericReqAndResp<VegaGhanaCashewRMINHistoryTranxResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String,
        @Query("werks") plantId: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

}
