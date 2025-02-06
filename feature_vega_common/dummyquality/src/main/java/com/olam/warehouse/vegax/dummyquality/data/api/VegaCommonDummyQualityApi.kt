package com.olam.warehouse.vegax.dummyquality.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.dummyquality.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaCommonDummyQualityApi {

    @POST("x-pre-processing/vega/qc/save-quality-details")
    suspend fun postDummySample(@Body postData: VegaCommonDummySampleModel): GenericMessage

    @GET("x-pre-processing/vega/qc/get-quality-list?")
    suspend fun getDummySample(
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("materialCode") materialCode: String,
        @Query("vendorCode") vendorCode: String,
        @Query("isDummy") dummySampleFlag: Boolean
    ): GenericReqAndResp<ResponseDummySample>


    @GET("x-pre-processing/vega/qc/get-quality-list?")
    suspend fun getDummySampleList(
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("materialCode") materialCode: String,
        @Query("vendorCode") vendorCode: String,
        @Query("isDummy") dummySampleFlag: Boolean
    ): GenericReqAndResp<List<ResponseDummySampleList>>

    @GET("x-pre-processing/vega/qc/get-quality-details")
    suspend fun getDummySampleDetails(
        @Query("key") key: String,
        @Query("id") plantId: String,
    ): GenericReqAndResp<ResponseDummySampleList>

    @GET("x-pre-processing/vega/qc/updateTempIdasInactive")
    suspend fun deleteDummySample(@Query("id") id:String):GenericReqAndResp<List<ResponseDummySampleList>>

    @GET("x-pre-processing/vega/qc/getVendorAndMaterialForDummyLot?")
    suspend fun getVendorAndMaterial(
        @Query("plantId") plantId: String,
    ): GenericReqAndResp<List<ResponseVendorAndMaterial>>
}
