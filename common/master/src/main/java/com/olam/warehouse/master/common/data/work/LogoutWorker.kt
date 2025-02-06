package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.MasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.LOGOUT_DATA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 4/7/2020.
 */
class LogoutWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //makeStatusNotification("Data downloading...", applicationContext)
        val key = getCurrentKey()
        val deviceId = inputData.getString(AppUtils.DEVICE_ID) ?: ""
        val api: MasterApi by inject()
        try {
            val response = api.updateLogout(deviceId).execute()
            if (response.isSuccessful) {
                val data = response.body()
                Result.success(workDataOf(LOGOUT_DATA to data?.message))
            } else if (response.body() == null) {
                Result.failure(workDataOf(LOGOUT_DATA to response.raw().message))
            } else {
                Result.failure(workDataOf(LOGOUT_DATA to response.raw().message))
            }
        } catch (error: Throwable) {
            Result.failure(workDataOf(LOGOUT_DATA to error.message))
        }
    }
}
