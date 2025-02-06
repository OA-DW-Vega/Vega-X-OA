package com.olam.warehouse.vegax.gateentrynigeria.data.api

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaPost
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaGateEntryNigeriaResponse
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaNigeriaGateEntryPostData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryNigeriaApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryPost: VegaGateEntryNigeriaPost): GenericReqAndResp<VegaGateEntryNigeriaResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-master/getOpenGRNTDetails")
    suspend fun fetchOpenGrntDetails(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialCodes: String,
        @Query("vendorCodes") vendorCodes: String,
        @Query("werks") plantID: String
    ): GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>

}
