package com.olam.warehouse.master.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.olam.warehouse.master.common.data.api.AppCenterApi
import com.olam.warehouse.presentation.utils.writeResponseBodyToDisk
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 7/27/2020.
 */
class FileDownloadWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), KoinComponent {

    override fun doWork(): Result {
        val api: AppCenterApi by inject()
        val filepath = inputData.getString(FILE_PATH) ?: ""
        val localPath = inputData.getString(LOCAL_PATH) ?: ""
        val directory = inputData.getString(DIRECTORY_PATH) ?: ""
        val response = api.downloadFile(
            filepath
        ).execute()

        if (response.isSuccessful) {
            val body = response.body()
            val isSuccess = writeResponseBodyToDisk(body, directory, localPath)
            return if (isSuccess) Result.success() else Result.failure()
        }
        return Result.retry()
    }
}
