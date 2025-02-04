package com.olam.warehouse.vegax.processingindiacoffee.data.api

import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBom
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnPost
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaProcessingFgrnResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaIndiaCoffeeInventoryStocks
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaIndiaCoffeeProcessingApi {

    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @POST("x-processing/vega/processing/fetchBom")
    suspend fun fetchBomList(@Body bomPostReq: VegaIndiaCoffeeProcessingRminBomPost): GenericReqAndResp<VegaProcessingRminBom>


    @POST("x-processing/vega/processing/createProcessOrder")
    suspend fun postCreatePo(@Body bomPostReq: VegaIndiaCoffeeProcessingCreatePoReq): GenericReqAndResp<VegaProcessingRminPo>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeProcessingQualityDetails>>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaIndiaCoffeeProcessingQualityDetails>>>

    @GET("x-master/getStock")
    suspend fun getStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCocoaRminLots>>

    @POST("x-processing/vega/processing/processOrdersDetails")
    suspend fun getFgrnGrades(@Body poReq: VegaIndiaCoffeeProcessOrderDetailsPostReq): GenericReqAndResp<List<VegaCocoaFgrnItemsGrades>>

    @POST("x-processing/vega/processing/processOrders")
    suspend fun getFgrnPoDetailsList(@Body vegaProcessingOrderReq: VegaIndiaCoffeeProcessingOrderReq): GenericReqAndResp<List<VegaFgrnProcessingOrder>>

    @POST("x-processing/vega/processing/processOrders")
    suspend fun fetchFgrnPoDetailsList(@Body vegaProcessingOrderReq: VegaIndiaCoffeeProcessingOrderReq): GenericReqAndResp<List<VegaCocoaFgrnItems>>

    @POST("x-processing/vega/processing/rmin_fgrn")
    suspend fun postRminDetails(@Body bomPostReq: VegaIndiaCoffeeRminProcessingPost): GenericReqAndResp<List<VegaIndiaCoffeeProcessingRminResponse>>

    @POST("x-processing/vega/processing/rmin_fgrn")
    suspend fun postFgrnDetails(@Body bomPostReq: VegaCocoaProcessingFgrnPost): GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaRminLots>>


    @POST("x-master/inventory/getInventoryDetails")
    suspend fun getIndiaCoffeeInventoryList(@Query("key") key: String)
            : GenericReqAndResp<VegaInventoryAndSyncModel>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaIndiaCoffeeInventoryStocks>>

    @GET("x-master/getLotQualityDetails")
    suspend fun fetchPreQualityDetails(
        @Query("key") key: String,
        @Query("charg") batchNo: String,
        @Query("material") materialNo: String
    ): GenericReqAndResp<List<VegaQualityPreParameter>>
}
