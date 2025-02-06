package com.olam.warehouse.vegax.qualitycoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaSavePrintTicket
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.*


interface VegaCoffeeQualityApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/getWBIdDetailsForPlant")
    suspend fun fetchLotDetail(
        @Query("key") currentKey: String,
        @Query("wbId") weighBridgeId: String
    ): GenericReqAndResp<List<VegaCoffeeLot>>

    @GET("x-master/getWBIdDetails")
    suspend fun fetchLotDetailForDual(
        @Query("key") currentKey: String,
        @Query("wbId") weighBridgeId: String
    ): GenericReqAndResp<VegaCoffeeLot>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @POST("x-pre-processing/vega/qc/save-quality-mtnr")
    suspend fun postQuality(@Body paramPost: VegaCoffeeQualityParamPost): GenericReqAndResp<VegaCoffeeQualityParamPost>

    @POST("x-pre-processing/vega/qc/save-quality")
    suspend fun postQualitySupplier(@Body paramPost: VegaCoffeeQualitySupplierParamPost): GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>

    @GET("x-master/getWSIdDetails")
    suspend fun getWeighScaleIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @GET("x-master/getWBIdDetails")
    suspend fun getWeighBridgeIdDetail(
        @Query("key") key: String,
        @Query("wbId") wbId: String
    ): GenericReqAndResp<VegaReceiving>

    @POST("/x-master/lotSequence")
    suspend fun updateLotSequence(@Body postData: NicaraguaUpdateTallySequencePost):
            GenericReqAndResp<NicaraguaUpdateTallySequencePost>

    @POST("/x-dispatch/reprint/upload/file")
    suspend fun saveprintformat(@Body printData : NicaraguaSavePrintTicket): GenericReqAndResp<NicaraguaSavePrintTicket>

    @POST("/x-dispatch/reprint/upload/file")
    fun saveprintformatWorker(@Body postData: NicaraguaSavePrintTicket): Call<GenericReqAndResp<NicaraguaSavePrintTicket>>

    @Multipart
    @POST("/x-dispatch/reprint/upload/file")
    suspend fun postprintformatdata(
        @Part("moduleName") moduleName : RequestBody,
        @Part("type") type : RequestBody,
        @Part("moduleNo") moduleNo : RequestBody,
        @Part("trasanctionNo") trasanctionNo : RequestBody,
        @Part("materialName") materialName : RequestBody,
        @Part("date") date : RequestBody,
        @Part("companyCode") companyCode : RequestBody,
        @Part("file") file : RequestBody
    ): Call<GenericReqAndResp<NicaraguaSavePrintTicket>>
}
