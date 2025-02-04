package com.olam.warehouse.vegax.bcapproveecuador.data.api

import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorBcApproveWBDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApprovePost
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorBcApproveResponse
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.bcapproveecuador.data.domain.model.VegaEcuadorApproveWbDetail
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaEcuadorBcApproveOffloadingApi {
    @GET("x-master/getApprovalWBList")
    suspend fun fetchWeighBridgeDetail(@Query("key") key: String,@Query("werks") plantId: String): GenericReqAndResp<List<VegaEcuadorBcApproveWBDetails>>

    @POST("x-pre-processing/vega/approve/ec/bc-approval")
    suspend fun postApproval(@Body postApprovalData: VegaEcuadorBcApprovePost): GenericReqAndResp<VegaEcuadorBcApproveResponse>

    @GET("x-master/getLotQualityDetails")
    suspend fun getQuality(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): GenericReqAndResp<List<VegaEcuadorApproveWbDetail>>
}
