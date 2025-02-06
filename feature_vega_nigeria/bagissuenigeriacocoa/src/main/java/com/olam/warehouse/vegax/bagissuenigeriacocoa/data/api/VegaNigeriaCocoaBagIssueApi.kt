package com.olam.warehouse.vegax.bagissuenigeriacocoa.data.api


import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagIssuePost
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaBagManagementResp
import com.olam.warehouse.vegax.bagissuenigeriacocoa.data.domain.model.VegaNigeriaCocoaCurrentBagsIssued
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaNigeriaCocoaBagIssueApi {

    @GET("x-master/getSupplierBagStock")
    suspend fun getCurrentBagsIssued(
        @Query("key") key: String,
        @Query("materialCode") materialCode: String,
        @Query("vendorCode") supplierCode: String
    ): GenericReqAndResp<List<VegaNigeriaCocoaCurrentBagsIssued>>

    @POST("x-pre-processing/vega/wb/post-supplier-bags")
    suspend fun postBagIssueData(@Body bagIssuePost: VegaNigeriaCocoaBagIssuePost): GenericReqAndResp<VegaNigeriaCocoaBagManagementResp>

    @GET("x-master/getBagMaterialStock")
    suspend fun getStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCocoaRminLots>>

}
