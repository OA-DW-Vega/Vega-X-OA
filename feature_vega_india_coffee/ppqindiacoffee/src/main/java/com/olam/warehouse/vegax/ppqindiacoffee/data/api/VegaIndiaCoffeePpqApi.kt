package com.olam.warehouse.vegax.ppqindiacoffee.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaPpqModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaIndiaCoffeePpqApi {

    @GET("x-processing/quality/getInspectionLots")
    suspend fun getInspectionLots(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaIndiaCoffeePpqInspectionLots>>

    @GET("x-processing/quality/getInspectionLotDetails")
    suspend fun getInspectionLotDetails(
        @Query("key") key: String,
        @Query("inspectionLotId") lotId: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails>

    @POST("x-processing/quality/saveInspectionLotDetails")
    suspend fun saveInspectionLotDetails(
        @Query("key") key: String,
        @Query("plantId") plant: String,
        @Body lotDetail: VegaIndiaCoffeePpqInspectionLotDetails
    ): GenericReqAndResp<VegaIndiaCoffeePpqPostResponse>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getIndiaCoffeeInventoryList(@Query("key") key: String): GenericReqAndResp<VegaPpqModel>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>
/*

    //    @GET("x-master/getStockByMaterials")
//    suspend fun getStockList(
//        @Query("key") currentKey: String,
//        @Query("materialCodes") materialList: ArrayList<String>
//    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getStockForQuality")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialTypes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>
*/

    @GET("x-master/getStockForQuality")
    suspend fun getStockList(
            @Query("key") currentKey: String,
            @Query("materialTypes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>
}
