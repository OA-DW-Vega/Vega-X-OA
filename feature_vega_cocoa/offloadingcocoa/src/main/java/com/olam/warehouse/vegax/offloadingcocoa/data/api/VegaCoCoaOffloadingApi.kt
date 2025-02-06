package com.olam.warehouse.vegax.offloadingcocoa.data.api

import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaCoCoaReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaQualityWBDetail
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCoCoaWeighScalePallet
import com.olam.warehouse.vegax.offloadingcocoa.data.domain.model.VegaCocoOffloadingSupplierPostRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCoCoaOffloadingApi {
    @POST("x-pre-processing/vega/wb/create-wbid")
    suspend fun postReceivingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaCoCoaReceivingMtnWrapper>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaCoCoaReceiving>>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBIdDetails")
    suspend fun geCocoaWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaCoCoaQualityWBDetail>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaCoCoaWeighScalePallet>>

    @POST("x-pre-processing/vega/qc/create-grn-mtnr")
    suspend fun postOffloadingDetail(@Body vegaOffloadingPost: VegaCoCoaOffloadingPostRequest): GenericReqAndResp<VegaCoCoaOffloadingPostRequest>

    @POST("x-pre-processing/vega/qc/create-grn-mtnr")
    fun postOffloadingDetailSync(@Body vegaOffloadingPost: VegaCoCoaOffloadingPostRequest): Call<GenericReqAndResp<VegaCoCoaOffloadingPostRequest>>

    @POST("x-pre-processing/vega/wb/create-supplier-wbid")
    suspend fun postOffloadingSupplierDetail(@Body vegaOffloadingPost: VegaCocoOffloadingSupplierPostRequest): GenericReqAndResp<VegaMtntResponse>
}
