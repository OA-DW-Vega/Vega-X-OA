package com.olam.warehouse.vegax.secretidcommon.data.api

import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.secretidcommon.data.domain.model.VegaSecretIdResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.ArrayList

interface VegaCommonSecretIdApi {

    @GET("x-master/getStockByPlant")
    suspend fun getAllStockByPlant(@Query("key") key: String):GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-master/getWBListDetails")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaQualityWBDetails>>

    @GET("x-master/isValidLotID")
    suspend fun getValidLotId(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material :String,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @GET("x-pre-processing/vega/qc/getSecretIdGen")
    suspend fun getSecretId(
        @Query("batchNo") batchNo: String,
        @Query("isSecretIdExists") isSecretIdExist: Boolean,
        @Query("matrialCode") material :String,
        @Query("plantFk") plant: String
    ): GenericReqAndResp<List<VegaSecretIdResponse>>

}
