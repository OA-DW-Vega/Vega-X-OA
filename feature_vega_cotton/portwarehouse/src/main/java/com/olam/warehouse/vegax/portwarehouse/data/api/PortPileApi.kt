package com.olam.warehouse.vegax.portwarehouse.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileSuccessResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface PortPileApi {

    @GET("x-ginning/port/pileManagement/getPileListsDetails")
    suspend fun getStorageLocationList(@Query("key") key: String): GenericReqAndResp<List<PortPileStorageLocationModel>>

    @GET("x-ginning/port/pileManagement/isBaleValidPile")
    suspend fun validateBale(
        @Query("key") key: String,
        @Query("baleId") baleId: String,
        @Query("pile") pileId: String
    ): GenericReqAndResp<PortPileBale>

    @POST("x-ginning/port/pileManagement/getBalePortPileDetails")
    suspend fun updateBaleToPile(
        @Query("key") key: String,
        @Body request: PortPileStorageLocationModel
    ): GenericReqAndResp<PortPileSuccessResponse>
}
