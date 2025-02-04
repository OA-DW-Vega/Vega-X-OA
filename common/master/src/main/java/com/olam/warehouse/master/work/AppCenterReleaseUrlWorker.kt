package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.AppCenterUrlApi
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_URL_OUTPUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 7/27/2020.
 */
class AppCenterReleaseUrlWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: AppCenterUrlApi by inject()
        val filepath = inputData.getString(UIUtils.RELEASE_URL_PATH) ?: ""
        try {
            val response = api.getReleaseUrl(
                filepath
            ).execute()

            if (response.isSuccessful) {
                val body = response.body()
                Result.success(
                    workDataOf(
                        RELEASE_URL_OUTPUT to (body?.download_url ?: ""),
                        UIUtils.RELEASE_VERSION to (body?.version ?: ""),
                        UIUtils.RELEASE_NOTES to (body?.release_notes ?: "")
                    )
                )
            } else {
                Result.failure(workDataOf(RELEASE_URL_OUTPUT to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(RELEASE_URL_OUTPUT to error.message))
        }
    }
}
