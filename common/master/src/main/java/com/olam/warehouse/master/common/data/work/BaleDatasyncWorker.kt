package com.olam.warehouse.master.common.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.olam.warehouse.master.common.data.api.GinningTransMasterApi
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.utils.BALE_DATA_SYNC
import com.olam.warehouse.presentation.utils.sleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Baskaran Kannan on 4/27/2020.
 */
class BaleDatasyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sleep()
        val api: GinningTransMasterApi by inject()
        try {
            val response = api.baleSync(getCurrentKey()).execute()
            if (response.isSuccessful) {
                val data = response.body()
                Result.success(workDataOf(BALE_DATA_SYNC to response.body()?.data?.message))
            } else if (response.body() == null) {
                Result.failure(workDataOf(BALE_DATA_SYNC to response.message()))
            } else {
                Result.failure(workDataOf(BALE_DATA_SYNC to response.message()))
            }
            Result.success()
        } catch (error: Throwable) {
            Result.failure(workDataOf(BALE_DATA_SYNC to error))
        }
    }
}
