package com.olam.warehouse.vegax.processingcoffee.data.api

import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBom
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.processingcoffee.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaCoffeeProcessingApi {
    @GET("x-master/getWBListDetails")
    suspend fun fetchTruckList(@Query("key") key: String): GenericReqAndResp<List<VegaOffloadingTrucks>>

    @POST("x-processing/vega/processing/fetchBom")
    suspend fun fetchBomList(@Body bomPostReq: VegaCoffeeProcessingRminBomPost): GenericReqAndResp<VegaProcessingRminBom>


    @POST("x-processing/vega/processing/createProcessOrder")
    suspend fun postCreatePo(@Body bomPostReq: VegaCoffeeProcessingCreatePoReq): GenericReqAndResp<VegaProcessingRminPo>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaCoffeeProcessingQualityDetails>>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaCoffeeProcessingQualityDetails>>>

    @GET("x-master/getStock")
    suspend fun getStocks(
        @Query("key") key: String,
        @Query("materialCode") material: String
    ): GenericReqAndResp<List<VegaCoffeeRminLots>>

    @POST("x-processing/vega/processing/processOrdersDetails")
    suspend fun getFgrnGrades(@Body poReq: VegaCoffeeProcessOrderDetailsPostReq): GenericReqAndResp<List<VegaCoffeeFgrnItemsGrades>>

    @POST("x-processing/vega/processing/processOrders")
    suspend fun getFgrnPoDetailsList(@Body vegaProcessingOrderReq: VegaCoffeeProcessingOrderReq): GenericReqAndResp<List<VegaFgrnProcessingOrder>>

    @POST("x-processing/vega/processing/processOrders")
    suspend fun fetchFgrnPoDetailsList(@Body vegaProcessingOrderReq: VegaCoffeeProcessingOrderReq): GenericReqAndResp<List<VegaCoffeeFgrnItems>>

    @POST("x-processing/vega/processing/rmin_fgrn")
    suspend fun postRminDetails(@Body bomPostReq: VegaCoffeeProcessingFgrnPost): GenericReqAndResp<List<VegaCoffeeProcessingRminResponse>>

    @POST("x-processing/vega/processing/rmin_fgrn")
    suspend fun postFgrnDetails(@Body bomPostReq: VegaCoffeeProcessingFgrnPost): GenericReqAndResp<List<VegaCoffeeProcessingFgrnResponse>>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCoffeeRminLots>>

    @GET("x-master/isValidLotID")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCoffeeRminLots>>
}
