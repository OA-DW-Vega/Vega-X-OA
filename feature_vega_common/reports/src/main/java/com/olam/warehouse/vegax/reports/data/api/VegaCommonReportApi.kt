package com.olam.warehouse.vegax.reports.data.api

import com.olam.warehouse.master.vega.model.VegaStockReconGetAllAuditData
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface VegaCommonReportApi {
    @GET("groups/{groupId}/reports/{reportId}")
    suspend fun getDataSet(@Path("groupId") groupId: String, @Path("reportId") reportId: String): VegaCommonReportDataSetModel

    @POST("GenerateToken")
    suspend fun getDatasetToken(@Body model: VegaCommonReportDataSetTokenRequest): VegaCommonReportDataSetTokenModel
}
