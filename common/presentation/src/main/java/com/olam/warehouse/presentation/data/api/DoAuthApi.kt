package com.olam.warehouse.presentation.data.api

import com.olam.warehouse.presentation.data.domain.model.DOAuthResponse
import com.olam.warehouse.presentation.data.domain.model.DoAuth
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
interface DoAuthApi {
    @POST("authorization/login")
    fun getAuthToken(@Body auth: DoAuth): Call<DOAuthResponse>
}
