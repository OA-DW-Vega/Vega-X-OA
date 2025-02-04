package com.olam.warehouse.vegax.ppqsesame.data.api

import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaPpqModel
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqPostResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
interface VegaSesamePpqApi {

    @GET("x-processing/quality/getInspectionLots")
    suspend fun getInspectionLots(
        @Query("key") key: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<List<VegaSesamePpqInspectionLots>>

    @GET("x-processing/quality/getInspectionLotDetails")
    suspend fun getInspectionLotDetails(
        @Query("key") key: String,
        @Query("inspectionLotId") lotId: String,
        @Query("plantId") plant: String
    ): GenericReqAndResp<VegaSesamePpqInspectionLotDetails>

    @POST("x-processing/quality/saveInspectionLotDetails")
    suspend fun saveInspectionLotDetails(
        @Query("key") key: String,
        @Query("plantId") plant: String,
        @Body lotDetail: VegaSesamePpqInspectionLotDetails
    ): GenericReqAndResp<VegaSesamePpqPostResponse>

    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getSesameInventoryList(@Query("key") key: String): GenericReqAndResp<VegaPpqModel>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>
}
