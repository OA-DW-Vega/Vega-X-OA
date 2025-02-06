package com.olam.warehouse.vegax.historytransactionsghanacocoa.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTranxResponse
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaHistoryTranxMtnr
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTranxResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface VegaGhanaCocoaHistoryTransactionsApi {

    @GET("/x-pre-processing/vega/wb/getPreprocessingTxnDetails")
    suspend fun fetchHistoryTranxGrn(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCocoaGRNHistoryTranxResponse>

    @GET("/x-pre-processing/vega/wb/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnr(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCocoaHistoryTranxMtnr>

    @GET("/x-dispatch/vega/dispatch/getMTNDeliveryTxnDetails")
    suspend fun fetchHistoryTranxMtnt(
        @Query("endDate") endDate: String,
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("startDate") startDate: String
    ) : GenericReqAndResp<VegaGhanaCocoaMtntHistoryTranxResponse>



}
