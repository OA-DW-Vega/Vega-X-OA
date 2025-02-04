package com.olam.warehouse.ginning.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.GinningTransMasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.DATA_SYNC
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Baskaran Kannan on 4/27/2020.
 */
class DataSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val key= getCurrentKey()
        val api: GinningTransMasterApi by inject()
        try {
            val response = api.dataSync(key).execute()
            if (response.isSuccessful) {
                val data = response.body()
                Result.success(workDataOf(DATA_SYNC to response.body()?.data?.message))
            } else if (response.body() == null) {
                Result.failure(workDataOf(DATA_SYNC to response.message()))
            } else {
                Result.failure(workDataOf(DATA_SYNC to response.message()))
            }
            Result.success()
        } catch (error: Throwable) {
            Result.failure(workDataOf(DATA_SYNC to error))
        }
    }
}
