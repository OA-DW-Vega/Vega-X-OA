package com.olam.warehouse.vegax.stockrecon.data.api

import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.master.vega.model.VegaStockReconIdDetails
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportAuditDetails
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportReconList
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconCreateReconIdReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataReq
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPostAuditDataResp
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconPrintRecipt
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaStockReconUpdateStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VegaStockReconApi {

    @GET("x-notification/getStockDetailsByPlant")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("plant") plant: String
    ): GenericReqAndResp<List<VegaEcuadorDispatchStocks>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLot(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaDispatchLots>>

    @GET("x-notification/isValidLotForPlant")
    suspend fun getLotInfo(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") material: String,
        @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaDispatchLots>>

    @GET("x-notification/vega/stock-reconciliation")
    suspend fun getStockReconInProgressAndCompetedList(
        @Query("plant") plant: String,
        @Query("storagelocation") storagelocation: String
    ): GenericReqAndResp<List<VegaStockReconIdDetails>>

    @POST("/x-notification/vega/stock-reconciliation")
    suspend fun getReconId(@Body request: VegaStockReconCreateReconIdReq): GenericReqAndResp<VegaStockReconIdDetails>

    @POST("/x-notification/vega/stock-reconciliation/{reconId}/audits")
    suspend fun postAuditData(
        @Body request: VegaStockReconPostAuditDataReq,
        @Path("reconId") reconId: String
    ): GenericReqAndResp<VegaStockReconPostAuditDataResp>

    @GET("/x-notification/vega/stock-reconciliation/{reconId}/audits")
    suspend fun getAllAuditData(@Path("reconId") reconId: String): GenericReqAndResp<List<VegaStockReconGetAllAuditData>>

    @PUT("x-notification/vega/updateStatusOfStockReconciliation")
    suspend fun updatReconIdStatus(
        @Query("reconid") reconId: String,
        @Query("status") status: String
    ): GenericReqAndResp<VegaStockReconUpdateStatus>

    @DELETE("x-notification/vega/stock-reconciliation/{reconId}/audits/{auditId}")
    suspend fun deleteAuditData(@Path("reconId") reconId: String, @Path("auditId") auditId: String): GenericMessage

    @PUT("x-notification/vega/updateStockReconReportUrl")
    suspend fun postPrintReceipt(@Body request: VegaStockReconPrintRecipt): GenericReqAndResp<VegaStockReconPrintRecipt>

    @GET("x-notification/vega/stock-reconciliation-report")
    suspend fun getReconReportReconList(
        @Query("plant") plantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): GenericReqAndResp<VegaReconReportReconList>

    @GET("x-notification/vega/stock-reconciliation/{reconId}/audits/{auditId}")
    suspend fun getReconReportAuditDetails(
        @Path("reconId") reconId: String,
        @Path("auditId") auditId: String
    ): GenericReqAndResp<VegaReconReportAuditDetails>

    @GET("/x-notification/vega/stock-reconciliation-report/{reconId}/audits")
    suspend fun getReconReportAuditList(@Path("reconId") reconId: String): GenericReqAndResp<List<VegaStockReconGetAllAuditData>>

    @GET("x-notification/vega/download-img/{reconId}/{auditId}")
    suspend fun getAuditImage(
        @Path("reconId") reconId: String,
        @Path("auditId") auditId: String
    ): GenericReqAndResp<String>

    @GET("x-notification/vega/download-report/{reconId}")
    suspend fun getReconReportImage(
        @Path("reconId") reconId: String,
    ): GenericReqAndResp<String>

}
