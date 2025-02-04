package com.olam.warehouse.vegax.createmapar.data.api

import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
interface ArApi {

    /* @GET("x-master/getExchangeRate")
     suspend fun getExchangeRate(
         @Query("appDate") date: String,
         @Query("key") key: String
     ): GenericReqAndResp<ExchangeRate>

     @POST("x-pre-processing/vega/grn/create-grn")
     suspend fun postGrnData(
         @Body receivingData: VegaNicaraguaGrnPost
     ): GenericReqAndResp<VegaNicaraguaGrnPost>*/

    @POST("x-master/ar")
    suspend fun saveLotDetails(@Body lotDetails: ArLotDetails): ArLotDetails

    @GET("x-master/ar/getARListByPlantId")
    suspend fun getLotDetails(@Query("plantId") plantId: String): List<ArLotDetails>
}
