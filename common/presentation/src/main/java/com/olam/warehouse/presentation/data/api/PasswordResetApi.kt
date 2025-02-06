package com.olam.warehouse.presentation.data.api

import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.ResetPasswordModel
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Baskaran Kannan on 7/3/2023.
 */
interface PasswordResetApi {

    @PUT("reset-password")
    fun resetPassword(@Body model: ResetPasswordModel,  @Header("Content-Type") type: String): Call<ResetPasswordModel>
}