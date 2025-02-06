package com.olam.warehouse.login.data.api

import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackTraceApi {

    @GET("x-master/getSourceLotDetails")
    suspend fun getSourceLotDetails(
        @Query("key") key: String,
        @Query("sourceLotId") sourceLotId: String,
    ): GenericReqAndResp<List<TrackTraceSourceLotDetails>>

    @GET("x-master/getVendorTransactionDetails")
    suspend fun getTranscationIdDetails(
        @Query("key") key: String,
        @Query("dwTransactionId") sourceLotId: String,
    ): GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>

}
