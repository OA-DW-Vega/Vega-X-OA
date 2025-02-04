package com.olam.warehouse.vegax.bagissueindiacoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssue
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssuePost
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeBagIssueResponse
import com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model.VegaIndiaCoffeeCurrentBagsIssued
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by Roshna Parambil on 11/29/2020.
 */

interface VegaIndiaCoffeeBagIssueApi {

/*    @GET("x-master/container/getContainerDetails?key=VEGA_CM_COCO_SAP&werks=2731")
    suspend fun getContainerData(
        @Query("key") key: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<List<VegaGrnWeighBridgeId>>*/

    @GET("x-master/getSupplierBagStock")
    suspend fun getCurrentBagsIssued(
        @Query("key") key: String,
        @Query("materialCode") materialCode: String,
        @Query("vendorCode") supplierCode: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeCurrentBagsIssued>>

    @POST("x-pre-processing/vega/wb/post-supplier-bags")
    suspend fun postBagIssueData(@Body bagIssuePost: VegaIndiaCoffeeBagIssuePost): GenericReqAndResp<VegaIndiaCoffeeBagIssueResponse>

}


