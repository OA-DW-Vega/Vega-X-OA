package com.olam.warehouse.vegax.grnnigeria.data.api

import com.olam.warehouse.master.common.model.VegaNigeriaCocoaOffloadingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Roshna Parambil on 9/9/2020.
 */

interface VegaNigeriaGrnApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeList(@Query("key") key: String): GenericReqAndResp<List<VegaGrnWeighBridgeId>>

    @POST("x-pre-processing/vega/gr/create-grn")
    suspend fun postGrn(@Body grnPost: VegaNigeriaGrnPost): GenericReqAndResp<VegaEcuadorGrnResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaNigeriaGRNQuality>>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQuality(@Body qualityPost: VegaCameroonQcPost): GenericReqAndResp<VegaNigeriaCocoaQualityApprovePostResponse>

    @POST("x-pre-processing/vega/wb/create-wbid-grn")
    suspend fun postNigeriaCocoa(@Body receivingData: VegaNigeriaCocoaOffloadingPost): GenericReqAndResp<VegaReceivingResponse>

    @GET("x-master/getSupplierBagStock")
    suspend fun getCurrentBagsIssued(
        @Query("key") key: String,
        @Query("materialCode") materialCode: String,
        @Query("vendorCode") supplierCode: String
    ): GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>
}
