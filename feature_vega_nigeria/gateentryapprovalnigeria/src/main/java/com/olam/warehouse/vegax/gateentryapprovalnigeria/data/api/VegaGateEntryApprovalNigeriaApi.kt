package com.olam.warehouse.vegax.gateentryapprovalnigeria.data.api

import com.olam.warehouse.master.common.model.VegaGateEntryApprovalPostResponse
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaDMSImageResponse
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaPost
import com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model.VegaGateEntryApprovalNigeriaResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Keerthi Santhanam on 3/5/2020.
 */
interface VegaGateEntryApprovalNigeriaApi {

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchWaitingTruckList(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaGateEntry>>

    @POST("x-pre-processing/vega/wb/create-truckwbid")
    suspend fun postGateEntryData(@Body gateEntryPost: VegaGateEntryApprovalNigeriaPost): GenericReqAndResp<VegaGateEntryApprovalNigeriaResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaReceivingMtnWrapper>

    @GET("x-pre-processing/vega/wb/getVegaWbDetails")
    suspend fun fetchVegaWbDetails(
        @Query("key") currentKey: String,
        @Query("status") status: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<VegaGateEntryApprovalPostResponse>

    @GET("x-master/getDMSUploadedImages")
    suspend fun getDMSUploadedImages(
        @Query("key") key: String,
        @Query("orgType") orgType: String,
        @Query("wbId") wbId: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<List<VegaDMSImageResponse>>

}
