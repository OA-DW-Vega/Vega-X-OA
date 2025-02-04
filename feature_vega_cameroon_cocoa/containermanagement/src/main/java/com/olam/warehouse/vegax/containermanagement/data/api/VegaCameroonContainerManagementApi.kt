package com.olam.warehouse.vegax.containermanagement.data.api

import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerPost
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerResponse
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainerInventoryModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by Roshna Parambil on 11/29/2020.
 */

interface VegaCameroonContainerManagementApi {

/*    @GET("x-master/container/getContainerDetails?key=VEGA_CM_COCO_SAP&werks=2731")
    suspend fun getContainerData(
        @Query("key") key: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<List<VegaGrnWeighBridgeId>>*/

    @POST("x-containerManagement/container/addContainer")
    suspend fun postContainerData(@Body containerPost: VegaCameroonAddContainerPost): GenericReqAndResp<VegaCameroonAddContainerResponse>

    @GET("x-containerManagement/container/getContainerDetails")
    suspend fun getContainerInventory(
        @Query("key") key: String,
        @Query("status") status: String,
        @Query("werks") werks: String
    ): GenericReqAndResp<VegaCameroonContainerInventoryModel>
}


