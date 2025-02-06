package com.olam.warehouse.vegax.gateentryghanacocoa.data.api

import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaCocoaWBMultiPlants
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaPost
import com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model.VegaGateEntryGhanaCocoaResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaGateEntryGhanaCocoaApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryCocoaPost: VegaGateEntryGhanaCocoaPost): GenericReqAndResp<VegaGateEntryGhanaCocoaResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBListDetailsForMTNT")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaCocoaDispatchWB>>


    @GET("/x-master/getWBDetailsforMultiPlants")
    suspend fun fetchWBListforMultiPlants(
        @Query("key") key: String,
        @Query("isMTNT") isMTNT: Boolean,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("plantList") plantList: List<String>,
        @Query("weighmentType") weighmentType: String
    ): GenericReqAndResp<List<VegaCocoaWBMultiPlants>>

    @GET("x-master/getWSIdDetails")
    suspend fun fetchSDWaybillNumber(
        @Query("key") key: String,
        @Query("wbId") wbID: String
    ): GenericReqAndResp<VegaCoffeeReceiving>

}
