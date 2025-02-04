package com.olam.warehouse.vegax.pilemanagementnigeria.data.api

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.pilemanagementnigeria.data.domain.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface VegaNigeriaPileManagementApi {

    @GET("x-master/getStockByMaterials")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getPilesByMaterials")
    suspend fun getStockPile(
        @Query("key") currentKey: String,
        @Query("materialCode") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaNigeriaPileSelectionModel>>

    @POST("x-master/lotSequence/getLatestPileSequence")
    suspend fun getCreatePile(
        @Query("key") currentKey: String,
        @Body plant: VegaNigeriaPilePlantDetails
    ): GenericReqAndResp<VegaNigeriaPileSequence>

    @GET("x-master/isValidLotID")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-dispatch/vega/dispatch/pilemgmt")
    suspend fun getPostPile(@Body postPileRequest: VegaNigeriaPilePostRequest): GenericReqAndResp<VegaNigeriaPileSuccessResponse>


}

