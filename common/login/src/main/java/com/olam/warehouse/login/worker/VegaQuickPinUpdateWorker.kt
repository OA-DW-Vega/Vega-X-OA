package com.olam.warehouse.login.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olam.warehouse.login.data.api.UsersApi
import com.olam.warehouse.login.data.domain.model.QuickPinModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 10/28/2020.
 */
@Suppress("UNCHECKED_CAST")
class VegaQuickPinUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: UsersApi by inject()
        val deviceId = inputData.getString(UIUtils.DEVICE_ID) ?: ""
        val quickPin = PreferenceHelper.get(Constants.QUICK_PIN, "")
        val isDevicePin = PreferenceHelper.get(Constants.IS_DEVICE_PIN, false)
        val quickPinModel =
            QuickPinModel(deviceId = deviceId, quickPin = quickPin, systemPin = isDevicePin)
        try {
            val response = api.updateQuickPin(quickPinModel).execute()
            if (response.isSuccessful) {
                val resp = response.body()
                val respData = response.body()?.data
                if (respData != null) {
                    Result.success()
                } else {
                    val msg = resp?.message ?: resp?.errors
                    Result.failure()
                }
            } else {
                Result.failure()
            }

        } catch (error: Throwable) {
            Result.failure()
        }
    }
}
