package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.AppCenterUrlApi
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_OUTPUT
import com.olam.warehouse.presentation.utils.UIUtils.RELEASE_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 7/27/2020.
 */
class AppCenterReleaseIdWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val api: AppCenterUrlApi by inject()
        val filepath = inputData.getString(UIUtils.RELEASE_ID) ?: ""
        try {
            val response = api.getReleaseId(
                filepath
            ).execute()

            if (response.isSuccessful) {
                val body = response.body()
                Result.success(
                    workDataOf(
                        RELEASE_OUTPUT to (body?.get(0)?.id ?: ""),
                        RELEASE_VERSION to (body?.get(0)?.short_version ?: "")
                    )
                )
            } else {
                Result.failure(workDataOf(RELEASE_OUTPUT to response.message()))
            }

        } catch (error: Throwable) {
            Result.failure(workDataOf(RELEASE_OUTPUT to error.message))
        }
    }
}
