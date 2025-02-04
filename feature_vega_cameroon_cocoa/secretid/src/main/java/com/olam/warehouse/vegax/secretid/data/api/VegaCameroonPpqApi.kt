package com.olam.warehouse.vegax.secretid.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.secretid.data.domain.model.VegaCameroonSecretId
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaCameroonSecretIdApi {

    @GET("x-master/getReprintDetailsForGRN")
    suspend fun getDashboardResult(
        @Query("key") key: String,
        @Query("isMTNT") isMTNT: Boolean,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): GenericReqAndResp<List<VegaCameroonSecretId>>

    //    @GET("x-master/getWBIdDetails")
    @GET("x-master/getWSIdDetails")
    suspend fun getWbWeightDetails(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaQualityWBDetails>

}
