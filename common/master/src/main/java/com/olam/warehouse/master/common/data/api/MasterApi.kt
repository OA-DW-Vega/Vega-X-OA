package com.olam.warehouse.master.common.data.api

import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.common.model.*
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.model.Key
import com.olam.warehouse.master.vega.entity.TrackTraceSourceLotDetails
import com.olam.warehouse.master.vega.entity.TrackTraceTransactionIdDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingCreatePoReq
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaTrackTraceFarmerData
import com.olam.warehouse.master.vega.model.TrackTraceModelTransactionIdDetails
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaDeliveryPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaVirtualDeliveryDetail
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaVirtualPostRequest
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorDeliveryPostResponse
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.master.vegaghana.model.*
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
interface MasterApi {
    /*@GET("x-master/getMasterData")
    suspend fun fetchMasterData(@Query("user") userName: String, @Query("warehouseId") warehouseID: String): GenericReqAndResp<List<Master>>
*/
    @POST("x-master/getMasterData")
    fun getMasterData(@Body selectedKeys: List<String>): Call<GenericReqAndResp<List<Master>>>

    @GET("x-master/getSourceLotDetails")
    fun getSourceLotDetails(
        @Query("key") key: String,
        @Query("sourceLotId") sourceLotId: String?,
    ): Call<GenericReqAndResp<List<TrackTraceSourceLotDetails>>>

    @GET("x-master/getVendorTransactionDetails")
     fun getTranscationIdDetails(
        @Query("key") key: String,
        @Query("dwTransactionId") transId: String?,
    ): Call<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>

//    @GET("x-master/getVendorTransactionDetails")
//    suspend fun getTranscationIdDetails(
//        @Query("key") key: String,
//    ): Call<GenericReqAndResp<List<TrackTraceModelTransactionIdDetails>>>

    @GET("x-master/getFarmarData")
    fun getTTFarmerData(@Query("key") key: String): Call<GenericReqAndResp<List<VegaTrackTraceFarmerData>>>

    @POST("x-master/getTransactionData")
    fun getTransMasterData(
        @Body Keys: List<String>,
        @Query("appDate") date: String,
        @Query("lastSyncTime") lastSyncTime: Long
    ): Call<GenericReqAndResp<List<TransactionMaster>>>

    @POST("x-master/inventory/syncStockFromSAP")
    fun syncInventory(@Query("plantId") plantId: String, @Query("materialCode") materialCode: String): Call<InventorySyncResponse>

    @POST("x-master/syncVendorMasterVega")
    fun syncVendor(
        @Body key: Key,
        @Query("isApp") isApp: Boolean
    ): Call<GenericReqAndResp<List<Vendor>>>

    @POST("x-master/syncVendorAdvanceItem")
    fun syncVendorAdvanceItem(@Query("companyCode") code: String): Call<GenericMessage>

    @POST("x-master/syncVendorCreditLimit")
    fun syncVendorCreditLimit(@Query("companyCode") code: String): Call<GenericMessage>

    @POST("x-master/syncExchangeRate")
    fun syncExchangeRate(
        @Query("appDate") date: String,
        @Query("companyCode") code: String
    ): Call<GenericMessage>

    @POST("x-pre-processing/vega/wb/create-wbid")
    fun postReceivingDetail(@Body receivingData: VegaReceivingPost): Call<GenericReqAndResp<VegaReceivingResponse>>

    @POST("x-pre-processing/vega/wb/create-wbid-grn")
    fun postGhanaCocoaOffloadingDetail(@Body receivingData: VegaGhanaCocoaOffloadingPost): Call<GenericReqAndResp<VegaReceivingResponse>>

    @POST("x-pre-processing/vega/qc/save-quality")
    fun postQualityDetail(@Body qualityPost: VegaQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    fun postMtnrQualityDetail(@Body qualityPost: VegaGhanaMtnrQualityPost): Call<GenericReqAndResp<VegaQualityPostResponse>>

    @POST("x-dispatch/vega/dispatch/create-wbid")
    fun postMtntData(@Body mtntData: VegaMtntPost): Call<GenericReqAndResp<VegaMtntResponse>>

    @POST("x-dispatch/vega/dispatch/create-delivery")
    fun postDeliveryDetail(@Body vegaDeliveryPost: VegaDeliveryPost): Call<GenericReqAndResp<VegaDeliveryPostResponse>>

    @POST("x-processing/vega/processing/createProcessOrder")
    fun postCreatePo(@Body bomPostReq: VegaProcessingCreatePoReq): Call<GenericReqAndResp<VegaProcessingRminPo>>

    //Cocoa

    @POST("x-dispatch/vega/dispatch/create-delivery")
    fun postDeliveryDetail(@Body vegaDeliveryPost: VegaCocoaDeliveryPost): Call<GenericReqAndResp<VegaDeliveryPostResponse>>

    @POST("x-pre-processing/vega/gr/create-grn")
    fun postGrn(@Body grnPost: VegaEcuadorGrnPost): Call<GenericReqAndResp<VegaEcuadorGrnResponse>>

    //Ecuador

    @POST("x-dispatch/vega/dispatch/auto-delivery")
    fun postDeliveryDetail(@Body vegaDeliveryPost: VegaEcuadorDeliveryPost): Call<GenericReqAndResp<VegaEcuadorDeliveryPostResponse>>


    @POST("x-dispatch/vega/dispatch/create-delivery/virtualplant")
    fun postVirtualDeliveryDetails(@Body vegaDeliveryPost: VegaCocoaVirtualPostRequest): Call<GenericReqAndResp<List<VegaCocoaVirtualDeliveryDetail>>>

    @GET("x-master/ubs/logout")
    fun updateLogout(@Query("deviceId") deviceID: String): Call<GenericReqAndResp<User>>

    //Ghana

    @POST("x-processing/vega/processing/rmin_fgrn")
    fun postOfflineProcessing(@Body bomPostReq: VegaGhanaOfflineProcessingFgrnPost): Call<GenericReqAndResp<List<VegaGhanaOfflineProcessingFgrnResponse>>>

    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
    fun postWeighScaleDeliveryDetails(@Body vegaDeliveryPost: VegaGhanaMtntDeliveryPost): Call<GenericReqAndResp<List<VegaGhanaMtntDeliveryDetail>>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchDOWeighBridgeDetail(@Query("key") currentKey: String): GenericReqAndResp<List<DOQualityWBDetails>>

    @POST("x-processing/vega/processing/createProcessOrder")
    fun postCreatePo(@Body bomPostReq: VegaGhanaProcessingCreatePoReq): Call<GenericReqAndResp<VegaProcessingRminPo>>


    @GET("x-master/getWBListDetails")
    fun fetchDOWeighBridgeDetailOnWorker(@Query("key") currentKey: String):Call<GenericReqAndResp< List<DOQualityWBDetails>>>
}
