package com.olam.warehouse.vegax.secretidnigeria.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaNigeriaSecretIdApi {

    @GET("x-master/getReprintDetailsForGRN")
    suspend fun getDashboardResult(
        @Query("key") key: String,
        @Query("isMTNT") isMTNT: Boolean,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): GenericReqAndResp<List<VegaNigeriaSecretId>>

    //    @GET("x-master/getWBIdDetails")
    @GET("x-master/getWSIdDetails")
    suspend fun getWbWeightDetails(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaQualityWBDetails>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaQualityWBDetails>

    @GET("/x-master/getWBDetailsforMultiPlants")
    suspend fun fetchWBListforMultiPlants(
        @Query("key") key: String,
        @Query("isMTNT") isMTNT: Boolean,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("plantList") plantList: List<String>,
        @Query("weighmentType") weighmentType: String
    ): GenericReqAndResp<List<VegaNigeriaSecretId>>


}
