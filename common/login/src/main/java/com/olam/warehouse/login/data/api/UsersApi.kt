package com.olam.warehouse.login.data.api

import com.olam.warehouse.login.data.domain.model.KeyCloakModel
import com.olam.warehouse.login.data.domain.model.QuickPinModel
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UsersApi {
    @GET("x-master/ubs/users")
    suspend fun getUserDetail(
        @Query("deviceId") deviceID: String,
        @Query("entity") entity: String
    ): User

    @POST("x-master/ubs/saveOrUpdatePin")
    fun updateQuickPin(@Body quickPinModel: QuickPinModel): Call<GenericReqAndResp<QuickPinModel>>

    @GET("x-master/keycloak/user/updateKeycloakId")
    suspend fun updateKeyCloakId(): GenericReqAndResp<KeyCloakModel>
}
