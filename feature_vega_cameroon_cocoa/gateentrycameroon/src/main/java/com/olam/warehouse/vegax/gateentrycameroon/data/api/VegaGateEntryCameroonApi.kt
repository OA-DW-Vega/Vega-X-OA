package com.olam.warehouse.vegax.gateentrycameroon.data.api

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonPost
import com.olam.warehouse.vegax.gateentrycameroon.data.domain.model.VegaGateEntryCameroonResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryCameroonApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryPost: VegaGateEntryCameroonPost): GenericReqAndResp<VegaGateEntryCameroonResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

}
