package com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.api

import com.olam.warehouse.master.common.model.VegaGhanaCocoaOffloadingPost
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaCoffeeReceivingMtnWrapper
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.ValidateNumber
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWInventoryModelResponse
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWManualModelResponse
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaReceivingPostLineItem
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaWeighScalePallet
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaGhanaCocoaOffloadingApi {

    @GET("x-master/getPurchaseOrders")
    suspend fun getPurchaseOrders(@Query("key") key: String): GenericReqAndResp<List<VegaEcuadorPurchaseOrder>>

    @POST("x-pre-processing/vega/wb/create-wbid-grn")
    suspend fun postGhanaCocoaOffloadingDetail(@Body receivingData: VegaGhanaCocoaOffloadingPost): GenericReqAndResp<VegaReceivingResponse>

/*************************************MTNR*****************************/

@POST("x-pre-processing/vega/wb/create-wbid")
suspend fun postReceivingDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>

    @POST("x-pre-processing-ca/sto/wb/create-wb")
    suspend fun postReceivingMtnDetail(@Body receivingData: VegaReceivingPost): GenericReqAndResp<VegaReceivingResponse>


    @POST("pre-processing-ca/wb/create-wbid")
    fun postReceiving(@Body receivingData: List<VegaReceiving>): Call<GenericReqAndResp<String>>

    @POST("x-pre-processing/vega/wb/create-wbid")
    fun postReceivingItem(@Body receivingData: VegaGhanaReceivingPostLineItem): Call<GenericReqAndResp<VegaReceivingResponse>>

    @GET("x-master/mtn-details")
    suspend fun fetchWarehouseWithMtns(@Query("key") currentKey: String): GenericReqAndResp<VegaCoffeeReceivingMtnWrapper>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBListNoWeightDetails")
    suspend fun fetchTruckInWeighBridgeDetail(@Query("key") key: String): GenericReqAndResp<List<VegaReceiving>>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-dispatch/vega/salesDispatch/getPalletDetails")
    suspend fun getPalletDetails(
        @Query("batchNumber") batchNumber: String,
        @Query("key") key: String,
        @Query("materialCode") materialCode: String
    ): GenericReqAndResp<List<VegaGhanaWeighScalePallet>>

//    @POST("x-dispatch/vega/dispatch/create-wsid")
//    suspend fun postOffloadingDetail(@Body vegaOffloadingPost: VegaSesameOffloadingPostRequest): GenericReqAndResp<VegaMtntResponse>

    @POST("x-pre-processing/vega/wb/create-wsid")
    suspend fun postOffloadingDetail(@Body vegaOffloadingPost: VegaGhanaOffloadingPostRequest): GenericReqAndResp<VegaMtntResponse>

    @POST("x-pre-processing/vega/wb/create-wsid")
    fun postOffloadingDetailSync(@Body vegaOffloadingPost: VegaGhanaOffloadingPostRequest): Call<GenericReqAndResp<VegaMtntResponse>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchMtnrWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getBagMaterialStock")
    suspend fun getStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCocoaRminLots>>

    @GET("x-master/getStock")
    suspend fun getLiveStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCocoaRminLots>>

    @GET("/x-pre-processing/vega/wb/validateTextAttributesFromSAP")
    suspend fun validateReceiptNumbers(@Query("key") key:String,
                                @Query("plantId") plantId:String,
                                @Query("challanNumber") challanNumber:String ,
                              @Query("wbType") wbType:String): GenericReqAndResp<ValidateNumber>


    @GET("/x-pre-processing/vega/wb/validateTextAttributesFromSAP")
    fun validateReceiptWhNumbers(@Query("key") key:String,
                                       @Query("plantId") plantId:String,
                                       @Query("challanNumber") challanNumber:String ,
                                       @Query("wbType") wbType:String): Call<GenericReqAndResp<ValidateNumber>>


    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>

    @GET("/x-pre-processing/dse/getLotDetailsByLotId")
   suspend fun getValidateLot(@Query("dseLotId")dseLotId:String,
                       @Query("key")key: String): GenericReqAndResp<VegaGRNDWLotManualModel>


    @GET("/x-pre-processing/dse/searchLotsFromInventory")
   suspend fun getDSCLotDetails(
        @Query("vendorSapCode") vendorSapCode:String,
        @Query("key")key:String
    ): GenericReqAndResp<List<VegaGRNDWLotManualModel>>


}
