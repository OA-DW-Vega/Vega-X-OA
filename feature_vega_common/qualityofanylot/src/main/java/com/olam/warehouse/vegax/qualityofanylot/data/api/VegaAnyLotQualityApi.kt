package com.olam.warehouse.vegax.qualityofanylot.data.api

import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaLotPost
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.model.VegaQualityPostLot
import com.olam.warehouse.master.common.model.VegaQualityPostResponse
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListResponse
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.ArrayList

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

interface VegaAnyLotQualityApi {


    @GET("x-pre-processing/vega/qc/get-quality-details")
    suspend fun getQualityDetailsById(
        @Query("key") key: String,
        @Query("id") id: String,
    ): GenericReqAndResp<VegaAnyLotListData>


    @GET("x-pre-processing/vega/qc/get-quality-list")
    suspend fun getQualityDetails(
        @Query("key") key: String,
        @Query("plantId") plantId: String,
        @Query("batchNumber") batchNumber: String,
        @Query("materialCode") materialCode:String="",
        @Query("isDummy") isDummy:Boolean=false
    ): GenericReqAndResp<List<VegaAnyLotListData>>

  @GET("x-master/getStockByPlant")
  suspend fun getAllStockByPlant(@Query("key") key: String):GenericReqAndResp<List<VegaCocoaDispatchLots>>

   @POST("x-pre-processing/vega/qc/save-quality-details")
   suspend fun postOrSaveQualityDetails(@Body request: VegaAnyLotQualityPostRequest):GenericReqAndResp<List<VegaAnyLotListData>>

   @GET("x-pre-processing/vega/qc/updateTempIdasInactive")
   suspend fun deleteTransactionItem(@Query("id") id:String):GenericReqAndResp<List<VegaAnyLotListData>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>


    @GET("x-master/isValidLotID")
    suspend fun getValidLotId(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material :String,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-pre-processing/vega/qc/calculate-batch-characteristics")
    suspend fun postQualityBatchPost(@Body vegaQualityNigeriaPost: VegaQualityNigeriaPost): GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>

}
