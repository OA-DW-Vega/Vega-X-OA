package com.olam.warehouse.presentation.data.api

import com.olam.warehouse.presentation.data.domain.model.Auth
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
interface AuthApi {
    @FormUrlEncoded
//    @POST("x-authorization/oauth/token")
    @POST("token")
    fun getAuthToken(
//        @Field("username") userName: String,
//        @Field("password") passWord: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
//        @Field("redirect_uri") uri: String,
        @Field("client_secret") clientSecret: String
    ): Call<Auth>

    @FormUrlEncoded
//    @POST("x-authorization/oauth/token")
    @POST("token")
    suspend fun getAuthDetail(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Auth
}
