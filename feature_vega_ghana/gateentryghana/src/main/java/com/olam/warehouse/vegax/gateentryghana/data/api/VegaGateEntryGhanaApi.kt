package com.olam.warehouse.vegax.gateentryghana.data.api

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentryghana.data.domain.model.VegaGateEntryGhanaPost
import com.olam.warehouse.vegax.gateentryghana.data.domain.model.VegaGateEntryGhanaResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryGhanaApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryPost: VegaGateEntryGhanaPost): GenericReqAndResp<VegaGateEntryGhanaResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

}
