package com.olam.warehouse.vegax.qualitycoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
    suspend fun updateLotSequence(@Body postData: NicaraguaUpdateTallySequencePost): GenericReqAndResp<NicaraguaUpdateTallySequencePost>
}
 /*@POST("x-pre-processing/vega/qc/save-quality-mtnr")*/
