package com.olam.warehouse.vegax.reports.data.api

import com.olam.warehouse.presentation.data.domain.model.Auth
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface VegaCommonReportAdTokenApi {
    @FormUrlEncoded
    @POST("{tanetId}/oauth2/token")
    suspend fun getAdToken(
        @Field("grant_type") grantType: String,
        @Field("resource") resource: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Path("tanetId") tanetId: String
    ): Auth
}
