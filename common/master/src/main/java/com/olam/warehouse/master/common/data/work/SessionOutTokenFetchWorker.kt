package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.olam.warehouse.master.BuildConfig.KEY_CLIENT_ID
import com.olam.warehouse.master.BuildConfig.KEY_CLIENT_SECRET
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.data.domain.MasterUseCase
import com.olam.warehouse.master.common.data.domain.model.ODMasterUseCase
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.data.api.PasswordResetApi
import com.olam.warehouse.presentation.data.domain.model.ResetPasswordModel
import com.olam.warehouse.presentation.data.domain.model.ResetPasswordNewModel
import com.olam.warehouse.presentation.data.remote.interceptor.getHeader
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.Constants.GRANT_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 29-12-2022.
 */

class SessionOutTokenFetchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Master data downloading...", applicationContext)
        sleep()
        val password = inputData.getString(Constants.PASS_WORD) ?: ""
        val isPasswordReset = inputData.getBoolean(Constants.RESET_PASSWORD, false)
        val userName = PreferenceHelper.get(Constants.USER_NAME, "")
        val token = PreferenceHelper.get(Constants.ACCESS_TOKEN, "")
        val api: AuthApi by inject()
        //val resetApi: PasswordResetApi by inject()
        try {
            if(isPasswordReset){
                val model = ResetPasswordNewModel()
                model.username = userName
                model.password = password
                val response = api.resetPassword(model).execute()
                //val response = resetApi.resetPassword(model, "text/plain").execute()
                if (response.isSuccessful) {
                    Result.success()
                } else if (response.body() == null) {
                    Result.failure(workDataOf(SESSION_OUT_DATA to response.raw().message))
                } else {
                    Result.failure(workDataOf(SESSION_OUT_DATA to response.raw().message))
                }
            }else {
                val response = api.getAuthDetailForSessionOut(
                    username = userName,
                    password = password,
                    grantType = GRANT_TYPE,
                    scope = Constants.SCOPE,
                    clientId = KEY_CLIENT_ID,
                    clientSecret = KEY_CLIENT_SECRET
                ).execute()
                if (response.isSuccessful) {
                    val data = response.body()
                    val access_token = data?.access_token
                    val refresh_token = data?.refresh_token
                    access_token?.let { PreferenceHelper.save(Constants.ACCESS_TOKEN, it) }
                    refresh_token?.let { PreferenceHelper.save(Constants.REFRESH_TOKEN, it) }
                    Result.success()
                } else if (response.body() == null) {
                    Result.failure(workDataOf(SESSION_OUT_DATA to response.raw().message))
                } else {
                    Result.failure(workDataOf(SESSION_OUT_DATA to response.raw().message))
                }
            }
        } catch (error: Throwable) {
            error.printStackTrace()
            Result.failure(workDataOf(SESSION_OUT_DATA to error.message))
        }
    }
}
